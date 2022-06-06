package de.hipphampel.eval.parser;

/*-
 * #%L
 * eval
 * %%
 * Copyright (C) 2022 Johannes Hampel
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static org.petitparser.parser.primitive.CharacterParser.anyOf;
import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.of;

import de.hipphampel.eval.Context;
import de.hipphampel.eval.definition.FunctionDefinition;
import de.hipphampel.eval.expr.Add;
import de.hipphampel.eval.expr.Div;
import de.hipphampel.eval.expr.Expression;
import de.hipphampel.eval.expr.FunctionCall;
import de.hipphampel.eval.expr.Mul;
import de.hipphampel.eval.expr.Neg;
import de.hipphampel.eval.expr.Pow;
import de.hipphampel.eval.expr.Sub;
import de.hipphampel.eval.expr.Value;
import de.hipphampel.eval.expr.ValueName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.petitparser.context.Failure;
import org.petitparser.context.Result;
import org.petitparser.context.Success;
import org.petitparser.parser.Parser;
import org.petitparser.parser.combinators.SettableParser;
import org.petitparser.parser.primitive.EpsilonParser;

/**
 * Collection of methods that produce basic {@link Parser Parsers}
 */
public class ParserFactory {

  private static final Parser digits = digit().plus();
  private static final Parser optDigits = digit().star();
  private static final Parser fraction = of('.').seq(digits);
  private static final Parser optFraction = of('.').seq(optDigits).optional();
  private static final Parser exponent = anyOf("eE").seq(anyOf("+-").optional(), digits);

  private ParserFactory() {
  }

  /**
   * Creates a {@link Parser} for the given {@code context} using the {@link ParseMode} of the
   * {@code context}.
   * <p>
   * See {@link #expressionParserStandard(Context)} and {@link #expressionParserSimplified(Context)}
   * for details
   *
   * @param context The {@link Context}
   * @return The parser
   */
  public static Parser expressionParser(Context<?, ?> context) {
    return expressionParser(context, context.parseMode());
  }

  /**
   * Creates a {@link Parser} for the given {@code context} and {@code mode}.
   * <p>
   * See {@link #expressionParserStandard(Context)} and {@link #expressionParserSimplified(Context)}
   * for details
   *
   * @param context The {@link Context}
   * @param mode    The mode
   * @return The parser
   */
  public static Parser expressionParser(Context<?, ?> context, ParseMode mode) {
    return switch (mode) {
      case STANDARD -> expressionParserStandard(context);
      case SIMPLIFIED -> expressionParserSimplified(context);
    };
  }

  /**
   * Creates a {@link Parser} for {@link Expression Expressions} using the mode
   * {@link ParseMode#SIMPLIFIED}.
   * <p>
   * The simplified parser has most aspects in common with the parser for mode {@code STANDARD}, but
   * it has some simplification named "convenience multiplication". In general, expressions parsed
   * by the {@link #expressionParserStandard(Context) expressionParserStandard} produce semantical
   * identical result, but in detail it has some differences regarding the multiplication, power of
   * and  sign operations.
   * <p>
   * The convenience multiplication means that you may leave out the multiplication operator between
   * two operands, so instead of writing {@code 3 * x} you may also write {@code 3x} to achieve the
   * same result.
   * <p>
   * There are some implications about that:
   * <ul>
   *   <li>The term {@code xy} becomes {@code x * y}, if you have defined only the variables {@code
   *   x} and {@code y}. But if you defined the variable {@code xy} as well, then {@code xy} is
   *   simply that variable. The reason is that parsing a variable name is always greedy, so it
   *   matches the longest possible variable name</li>
   *   <li>Convenience multiplication in combination with the power of operator is intuitive, but
   *   follows no strict precedence order, because there are exceptions. Consider there are the
   *   variables {@code x} and {@code y}, then the following applies:
   *   <ul>
   *     <li>If the exponent is written in convenience multiplication, the term {@code x^2y}
   *     is effectively {@code 2^(2*y)}. This might be missleading in more complex situals like
   *     {@code 2^(x+1)(x-1)} (which is {@code 2^((x+1)*(x-1)}</li>
   *     <li>If the basis is written in convenience multiplication, the terms are evaluated as
   *     expected: {@code xy^2} is the same as {@code x*y^2}</li>
   *   </ul>
   *   </li>
   *   <li>The sign has a lower priority than power of. This means that {@code -2^2} is {@code
   *   -(2^2)}</li>
   * </ul>
   *
   * @param context The {@link Context} to use.
   * @return The {@link Parser}
   */
  public static Parser expressionParserSimplified(Context<?, ?> context) {
    SettableParser primitiveGroup = SettableParser.undefined();
    SettableParser signGroup = SettableParser.undefined();
    SettableParser conMulGroup = SettableParser.undefined();
    SettableParser powGroup = SettableParser.undefined();
    SettableParser factorGroup = SettableParser.undefined();
    SettableParser sumGroup = SettableParser.undefined();

    Parser valueParser = valueParser(false, context.precision());
    Parser parenthesisParser = of('(').trim().seq(sumGroup, of(')').trim())
        .map((List<Expression> values) -> values.get(1));
    Parser definitionParser = definitionParser(context, sumGroup);
    Parser conMulParser = listParser(null, signGroup, primitiveGroup, true)
        .map(convenienceMultiplicationMapper());
    Parser powParser = binaryRightAssocOperatorParser(signGroup, conMulGroup,
        Map.of('^', Pow::new));
    Parser conMulPowParser = conMulParser.seq(of('^').trim(), powGroup)
        .map((List<Object> values) -> new Mul(
            ((Mul) values.get(0)).left(),
            new Pow(((Mul) values.get(0)).right(), (Expression) values.get(2))));
    Parser signParser = unaryOperatorParser(powGroup,
        Map.of('-', Neg::new,
            '+', Function.identity()));

    Parser factorParser = binaryLeftAssocOperatorParser(powGroup, powGroup,
        Map.of('*', Mul::new,
            '/', Div::new));
    Parser sumParser = binaryLeftAssocOperatorParser(factorGroup, factorGroup,
        Map.of('+', Add::new,
            '-', Sub::new));

    primitiveGroup.set(valueParser.or(definitionParser, parenthesisParser));
    signGroup.set(signParser.or(primitiveGroup));
    conMulGroup.set(conMulParser.or(signGroup));
    powGroup.set(powParser.or(conMulPowParser, conMulGroup));
    factorGroup.set(factorParser.or(powGroup, powGroup));
    sumGroup.set(sumParser.or(factorGroup));
    return sumGroup.get();
  }

  /**
   * Creates a {@link Parser} for {@link Expression Expressions} using the mode
   * {@link ParseMode#STANDARD}.
   * <p>
   * The parser is a normal expression parser, providing numbers, variables, function calls, the
   * common arithmetic expressions with infix notation and grouping of expressions via parenthesis.
   * <p>
   * It is called "standard", since it follows conventions quite common to most simple parsers.
   * <p>
   * The following table gives an overview about the recognized expressions and their priority
   * (expressions mentioned first have higher priority than the others.
   *
   * <table>
   *   <caption>Examples</caption>
   *   <thead>
   *      <tr>
   *        <th>Type</th>
   *        <th>Description</th>
   *        <th>Examples</th>
   *      </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <td>Value</td>
   *       <td>Floating point number, including numbers with e-notation</td>
   *       <td>{@code 1.2}, or {@code 1.2e-34}</td>
   *     </tr>
   *     <tr>
   *       <td>ValueName</td>
   *       <td>ValueName names; must be known to the {@code context} at time of parsing</td>
   *       <td>{@code ab}, or {@code x}</td>
   *     </tr>
   *     <tr>
   *       <td>Unary {@code +} and {@code -}</td>
   *       <td>Positive or negative sign</td>
   *       <td>{@code -ab}, or {@code +123}</td>
   *     </tr>
   *     <tr>
   *       <td>{@code (...)}</td>
   *       <td>Grouping with parenthesis</td>
   *       <td>{@code (2+3)*5}</td>
   *     </tr>
   *     <tr>
   *       <td>{@code fn(...)}</td>
   *       <td>Function calls; function anmes must be known to the {@code context} at time of parsing</td>
   *       <td>{@code sin(x)}</td>
   *     </tr>
   *     <tr>
   *       <td>{@code ^}</td>
   *       <td>Power of, right associative</td>
   *       <td>{@code 2^3}</td>
   *     </tr>
   *     <tr>
   *       <td>Binaray {@code *} and {@code /}</td>
   *       <td>Multiplication and division, left associative</td>
   *       <td>{@code 2 * 3 / 4}</td>
   *     </tr>
   *     <tr>
   *       <td>Binary {@code +} and {@code -}</td>
   *       <td>Addtion and substraction, left associative</td>
   *       <td>{@code 2 + 3 - 4}</td>
   *     </tr>
   *   </tbody>
   * </table>
   * <p>
   * Please obey the following:
   * <ul>
   *   <li>The sign operators have a higher priority than power of. This means that {@code -2^3}
   *   is effectively the same like {@code (-2)^3}.</li>
   * </ul>
   *
   * @param context The {@link Context} to use.
   * @return The {@link Parser}
   */
  public static Parser expressionParserStandard(Context<?, ?> context) {
    SettableParser primitiveGroup = SettableParser.undefined();
    SettableParser powGroup = SettableParser.undefined();
    SettableParser factorGroup = SettableParser.undefined();
    SettableParser sumGroup = SettableParser.undefined();

    Parser valueParser = valueParser(true, context.precision());
    Parser parenthesisParser = of('(').trim().seq(sumGroup, of(')').trim())
        .map((List<Expression> values) -> values.get(1));
    Parser definitionParser = definitionParser(context, sumGroup);
    Parser signParser = unaryOperatorParser(primitiveGroup,
        Map.of('-', Neg::new,
            '+', Function.identity()));
    Parser powParser = binaryRightAssocOperatorParser(primitiveGroup, powGroup,
        Map.of('^', Pow::new));
    Parser factorParser = binaryLeftAssocOperatorParser(powGroup, powGroup,
        Map.of('*', Mul::new,
            '/', Div::new));
    Parser sumParser = binaryLeftAssocOperatorParser(factorGroup, factorGroup,
        Map.of('+', Add::new,
            '-', Sub::new));

    primitiveGroup.set(valueParser.or(signParser, definitionParser, parenthesisParser));
    powGroup.set(powParser.or(primitiveGroup));
    factorGroup.set(factorParser.or(powGroup));
    sumGroup.set(sumParser.or(factorGroup));
    return sumGroup.get();
  }

  private static Function<List<Expression>, Expression> convenienceMultiplicationMapper() {
    return values -> {
      Expression result = values.get(0);
      for (int i = 1; i < values.size(); i++) {
        result = new Mul(result, values.get(i));
      }
      return result;
    };
  }


  /**
   * Factory method for creating {@link Parser Parsers} that parse a function calla and value
   * names.
   * <p>
   * A Function call has the general format {@code <function> '(' <arg> ',' ...  ',' <arg>)},
   * whereas the function refers to a {@link FunctionDefinition} that also describes the number of
   * expected arguments. A value name refers to a
   * {@link de.hipphampel.eval.definition.ValueDefinition}
   *
   * @param context        {@link Context} that also knows the {@code FunctionDefinitions} and
   *                       {@code ValueDefinitions}
   * @param argumentParser The {@code Parser} for parsing the arguments of functions.
   * @return A parser producing an {@link Expression}
   */
  public static Parser definitionParser(Context<?, ?> context, Parser argumentParser) {
    return new DefinitionParser(context, argumentParser);
  }

  /**
   * Factory method for creating {@link Parser Parsers} that parse a list of elements.
   * <p>
   * The returned parser is able to parse a non empty list of objects produced by the
   * {@code headParser} for the first element and {@code tailParser} for all the others, optionally
   * separated by {@code separator}.
   *
   * @param separator    The separator character or {@code null}
   * @param headParser   The {@code Parser} to parse the first element
   * @param tailParser   The {@code Parser} to parse the other elements than the first
   * @param tailRequired Flag indicating, whether more than one element required
   * @return A parser producing a {@code List}
   */
  public static Parser listParser(Character separator, Parser headParser, Parser tailParser,
      boolean tailRequired) {
    Parser sep = separator == null ? new EpsilonParser() : of(separator).trim();
    Parser tail = sep.seq(tailParser);
    if (tailRequired) {
      return headParser.seq(tail.plus()).map(listParserMapper());
    } else {
      return headParser.seq(tail.star()).map(listParserMapper());
    }
  }

  @SuppressWarnings("unchecked")
  private static Function<List<Object>, List<Object>> listParserMapper() {
    return values -> {
      List<Object> result = new ArrayList<>();
      result.add(values.get(0));
      List<List<Object>> tail = (List<List<Object>>) values.get(1);
      tail.stream().map(l -> l.get(1)).forEach(result::add);
      return result;
    };
  }

  /**
   * Factory method for creating {@link Parser Parsers} for binary right associative operators.
   * <p>
   * The returned parser is able to parse expressions like
   * {@code '<left> <operator> <left> ... <operator> <right>'}, whereas the operator is a single
   * character and the arguments a {@link Expression Expressions}. The result is an
   * {@code Expression} as well. The evaluation order is from left to right; the last expression can
   * be parsed via the {@code rightParser}, all others via the {@code leftParser}
   * <p>
   * Which {@code Expressions} are created depends on the {@code operatorMapping} parameter that
   * assigns to each operator a function, that describes how to calculate the result.
   * <p>
   * An example could be:
   * <pre><code>
   *     binaryRightAssocOperatorParser(
   *         argumentParser,
   *         argumentParser,
   *         Map.of(
   *             '-', Sub::new,
   *             '+', Add::new
   *         )
   *     );
   *
   * </code></pre>
   *
   * @param leftParser      The {@code Parser} to use to parse the all except the right most
   *                        argument, must produce an {@code Expression}
   * @param rightParser     The {@code Parser} to use to parse the right most arguments, must
   *                        produce an {@code Expression}
   * @param operatorMapping A map that assigns to each operator character a function that describes
   *                        how to produce the result.
   * @return A parser producing an {@code Expression}
   */
  public static Parser binaryRightAssocOperatorParser(Parser leftParser, Parser rightParser,
      Map<Character, BiFunction<Expression, Expression, Expression>> operatorMapping) {
    String ops = operatorMapping.keySet().stream().map(Object::toString)
        .collect(Collectors.joining());
    Parser opsParser = anyOf(ops).trim();

    return leftParser.seq(opsParser.seq(leftParser).starGreedy(opsParser.seq(rightParser)),
            opsParser.seq(rightParser))
        .map(binaryRightAssocOpParserMapper(operatorMapping));
  }

  @SuppressWarnings("unchecked")
  private static Function<List<Object>, Expression> binaryRightAssocOpParserMapper(
      Map<Character, BiFunction<Expression, Expression, Expression>> operatorMapping) {
    return values -> {
      Expression left = (Expression) values.get(0);
      List<List<Object>> middle = (List<List<Object>>) values.get(1);
      Expression right = (Expression) ((List<Object>) values.get(2)).get(1);
      Character op = (Character) ((List<Object>) values.get(2)).get(0);
      for (int i = middle.size() - 1; i >= 0; i--) {
        List<Object> current = middle.get(i);
        right = operatorMapping.get(op).apply((Expression) current.get(1), right);
        op = (Character) current.get(0);
      }
      right = operatorMapping.get(op).apply(left, right);
      return right;
    };
  }


  /**
   * Factory method for creating {@link Parser Parsers} for binary left associative operators.
   * <p>
   * The returned parser is able to  parse expressions like
   * {@code '<left> <operator> <right> ... <operator> <right>'}, whereas the operator is a single
   * character and the arguments a {@link Expression Expressions}. The result is an
   * {@code Expression} as well. The evaluation order is from left to right; the first expression
   * can be parsed via the {@code leftParser}, all following via the {@code rightParser}
   * <p>
   * Which {@code Expressions} are created depends on the {@code operatorMapping} parameter that
   * assigns to each operator a function, that describes how to calculate the result.
   * <p>
   * An example could be:
   * <pre><code>
   *     binaryLeftAssocOperatorParser(
   *         argumentParser,
   *         argumentParser,
   *         Map.of(
   *             '-', Sub::new,
   *             '+', Add::new
   *         )
   *     );
   *
   * </code></pre>
   *
   * @param leftParser      The {@code Parser} to use to parse the left most argument, must produce
   *                        an {@code Expression}
   * @param rightParser     The {@code Parser} to use to parse the all other arguments, must produce
   *                        an {@code Expression}
   * @param operatorMapping A map that assigns to each operator character a function that describes
   *                        how to produce the result.
   * @return A parser producing an {@code Expression}
   */
  public static Parser binaryLeftAssocOperatorParser(Parser leftParser, Parser rightParser,
      Map<Character, BiFunction<Expression, Expression, Expression>> operatorMapping) {
    String ops = operatorMapping.keySet().stream().map(Object::toString)
        .collect(Collectors.joining());
    Parser opsParser = anyOf(ops).trim();
    return leftParser.seq(opsParser, rightParser.separatedBy(opsParser))
        .map(binaryLeftAssocOpParserMapper(operatorMapping));
  }

  @SuppressWarnings("unchecked")
  private static Function<List<Object>, Expression> binaryLeftAssocOpParserMapper(
      Map<Character, BiFunction<Expression, Expression, Expression>> operatorMapping) {
    return values -> {
      Expression left = (Expression) values.get(0);
      Character op = (Character) values.get(1);
      List<Object> rights = (List<Object>) values.get(2);
      left = operatorMapping.get(op).apply(left, (Expression) rights.get(0));
      for (int i = 1; i < rights.size(); i += 2) {
        op = (Character) rights.get(i);
        left = operatorMapping.get(op).apply(left, (Expression) rights.get(i + 1));
      }
      return left;
    };
  }

  /**
   * Factory method for creating {@link Parser Parsers} for unary operators.
   * <p>
   * The returned parser is able to  parse expressions like {@code '<operator> <argument>'}, whereas
   * the operator is a single character and the argument an {@link Expression}. The result is an
   * {@code Expression} as well.
   * <p>
   * Which {@code Expressions} are created depends on the {@code operatorMapping} parameter that
   * assigns to each operator a function, that describes how to calculate the result.
   * <p>
   * An example could be:
   * <pre><code>
   *     unaryOperatorParser(
   *         argumentParser,
   *         Map.of(
   *             '-', Neg::new,
   *             '+', Function.identity()
   *         )
   *     );
   *
   * </code></pre>
   *
   * @param argParser       The {@code Parser} to use for the argument, must produce an
   *                        {@code Expression}
   * @param operatorMapping A map that assigns to each operator character a function that describes
   *                        how to produce the result.
   * @return A parser producing an {@code Expression}
   */
  public static Parser unaryOperatorParser(Parser argParser,
      Map<Character, Function<Expression, Expression>> operatorMapping) {
    String ops = operatorMapping.keySet().stream().map(Object::toString)
        .collect(Collectors.joining());
    return anyOf(ops).trim().seq(argParser)
        .map(unaryOpParserMapper(operatorMapping));
  }

  private static Function<List<Object>, Expression> unaryOpParserMapper(
      Map<Character, Function<Expression, Expression>> operatorMapping) {
    return values -> {
      Character op = (Character) values.get(0);
      Expression arg = (Expression) values.get(1);
      return operatorMapping.get(op).apply(arg);
    };
  }


  /**
   * Creates a {@link Parser} that recognizes number literals.
   * <p>
   * The parser returns these literals as {@link Value Values}.
   * <p>
   * The patterns recognized by the parser depend on the flag {@code allowExponent}, if it is
   * {@code true}, the number might have the a {@code 'e&lt;exponent>'} or {@code 'E&lt;exponent>'}
   * after the significant part indicating the exponent to the base of {@code 10}. Note that the
   * sign of the number is <strong>not</strong> part of the parser.
   * <p>
   * All in all, the allowed notation are the same like in Java. So, the following are valid
   * examples for tokens accepted:
   * <table>
   *   <caption>Examples</caption>
   *    <thead>
   *      <tr>
   *        <th>Input</th>
   *        <th>Token with exponent</th>
   *        <th>Token without exponent</th>
   *      </tr>
   *    </thead>
   *    <tbody>
   *      <tr>
   *        <td>12.34</td>
   *        <td>12.34</td>
   *        <td>12.34</td>
   *      </tr>
   *      <tr>
   *        <td>12.</td>
   *        <td>12.</td>
   *        <td>12.</td>
   *      </tr>
   *      <tr>
   *        <td>12</td>
   *        <td>12</td>
   *        <td>12</td>
   *      </tr>
   *      <tr>
   *        <td>.34</td>
   *        <td>.34</td>
   *        <td>.34</td>
   *      </tr>
   *      <tr>
   *        <td>12.34e56</td>
   *        <td>12.34e56</td>
   *        <td>12.34</td>
   *      </tr>
   *      <tr>
   *        <td>12.34E+56</td>
   *        <td>12.34E+56</td>
   *        <td>12.34</td>
   *      </tr>
   *      <tr>
   *         <td>12.34e-56</td>
   *         <td>12.34e-56</td>
   *         <td>12.34</td>
   *      </tr>
   *   </tbody>
   * </table>
   *
   * @param allowExponent {@code true}, if allowing an exponent
   * @param precision     The precision of the numbers to create
   * @return The parser, producing a {@code Value}
   */
  public static Parser valueParser(boolean allowExponent, long precision) {
    Parser baseParser;
    if (allowExponent) {
      baseParser = digits.seq(optFraction, exponent.optional())
          .or(optDigits.seq(fraction, exponent.optional()));
    } else {
      baseParser = digits.seq(optFraction).or(optDigits.seq(fraction));
    }
    return baseParser.flatten()
        .trim()
        .map((String str) -> new Value(new Apcomplex(new Apfloat(str, precision))));
  }


  /**
   * Creates a {@link Parser} that matches the longest of the given {@code names}.
   * <p>
   * The {@code Parser} is greedy, so it matches the longest {@code name} from {@code names}. So if
   * {@code names} is {@code ['a', 'abc']}, the input {@code 'abc'} will match {@code 'abc'} instead
   * of {@code 'a'}
   *
   * @param names   The names to match
   * @param message The fail message
   * @return The parser, producing a {@code String}
   */
  public static Parser nameParser(Collection<String> names, String message) {
    return new NameParser(new HashSet<>(names), message).trim();
  }

  static class DefinitionParser extends Parser {

    private final Context<?, ?> context;
    private final Parser argumentParser;

    DefinitionParser(Context<?, ?> context, Parser argumentParser) {
      this.context = context;
      this.argumentParser = argumentParser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result parseOn(org.petitparser.context.Context context) {
      Set<String> names = new HashSet<>(this.context.functionNames());
      names.addAll(this.context.valueNames());
      Parser nameParser = new NameParser(names, "definition expected").trim();
      Result result = nameParser.parseOn(context);
      if (result.isFailure()) {
        return result;
      }

      String name = result.get();
      if (this.context.valueNames().contains(name)) {
        return new Success(result.getBuffer(), result.getPosition(), new ValueName(name));
      }

      FunctionDefinition definition = this.context.function(name);
      Parser argumentListParser = of('(').trim()
          .seq(listParser(',', argumentParser, argumentParser, false), of(')').trim())
          .map((List<Object> values) -> (List<Expression>) values.get(1));
      result = argumentListParser.parseOn(result);
      if (result.isFailure()) {
        return result;
      }

      List<Expression> args = result.get();
      if (args.size() < definition.minArgs() || args.size() > definition.maxArgs()) {
        return new Failure(result.getBuffer(), result.getPosition(), "Invalid paramter count");
      }

      return new Success(result.getBuffer(), result.getPosition(), new FunctionCall(name, args));
    }

    @Override
    public Parser copy() {
      return new DefinitionParser(context, argumentParser);
    }
  }

  static class NameParser extends Parser {

    private final Set<String> names;
    private final String message;


    NameParser(Set<String> names, String message) {
      this.names = names;
      this.message = message;
    }

    @Override
    public Result parseOn(org.petitparser.context.Context context) {
      String buffer = context.getBuffer();
      int pos = context.getPosition();

      String exactMatch = null;
      Set<String> available = names;
      for (int i = 0; pos + i < buffer.length() && !available.isEmpty(); i++) {
        char ch = buffer.charAt(pos + i);
        int ai = i;
        available = available.stream()
            .filter(s -> s.length() > ai)
            .filter(s -> s.charAt(ai) == ch)
            .collect(Collectors.toSet());
        if (available.size() == 1 && available.iterator().next().length() == ai + 1) {
          return context.success(buffer.substring(pos, pos + i + 1), pos + ai + 1);
        } else {
          exactMatch = available.stream().filter(s -> s.length() == ai + 1).findFirst()
              .orElse(exactMatch);
        }
      }
      return exactMatch == null ? context.failure(message)
          : context.success(exactMatch, pos + exactMatch.length());
    }

    @Override
    public Parser copy() {
      return new NameParser(names, message);
    }
  }

}
