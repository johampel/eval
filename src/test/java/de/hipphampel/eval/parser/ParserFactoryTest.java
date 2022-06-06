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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.eval.ApcomplexContext;
import de.hipphampel.eval.Context;
import de.hipphampel.eval.definition.FunctionDefinition;
import de.hipphampel.eval.expr.Expression;
import de.hipphampel.eval.expr.Value;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apfloat.Apcomplex;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;

public class ParserFactoryTest {

  @ParameterizedTest
  @CsvSource({
      // names,              input,   isSuccess, token, position
      "'a,b',                'ab',    true,      a,     1",
      "'a,b',                ' a b',  true,      a,     3",
      "'a,b',                'xb',    false,     ,      0",
      "'ab,bc',              'bc',    true,      bc,    2",
      "'ab,bc',              'bc',    true,      bc,    2",
      "'a,abc',              'ab',    true,      a,     1",
      "'a,ab',               'abc',   true,      ab,    2",
      "'a,abc',              'abc',   true,      abc,   3",
  })
  public void nameParser(String namesByComma, String input, boolean isSuccess, String token,
      int position) {
    Parser parser = ParserFactory.nameParser(Arrays.asList(namesByComma.split(",")), "MyError");

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat((String) result.get()).isEqualTo(token);
    } else {
      assertThat(result.getMessage()).isEqualTo("MyError");
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // allowExponent,     input,        isSuccess, token,                        position
      "true,                '12',         true,      'Value[value=1.2e1]',         2",
      "true,                '12. ',       true,      'Value[value=1.2e1]',         4",
      "true,                ' 12.34',     true,      'Value[value=1.234e1]',       6",
      "true,                '.34',        true,      'Value[value=3.4e-1]',        3",
      "true,                '12e34 ',     true,      'Value[value=1.2e35]',        6",
      "true,                ' 12.E+34',   true,      'Value[value=1.2e35]',        8",
      "true,                '12.34e-34',  true,      'Value[value=1.234e-33]',     9",
      "true,                '12e ',       true,      'Value[value=1.2e1]',         2",
      "true,                '.',          false,     ,                             1",
      "true,                ' .e',        false,     ,                             2",
      "false,               'e1',         false,     ,                             0",
      "false,               '12 ',        true,      'Value[value=1.2e1]',         3",
      "false,               ' 12.',       true,      'Value[value=1.2e1]',         4",
      "false,               '12.34',      true,      'Value[value=1.234e1]',       5",
      "false,               '.34 ',       true,      'Value[value=3.4e-1]',        4",
      "false,               ' 12e34',     true,      'Value[value=1.2e1]',         3",
      "false,               '12.E+34',    true,      'Value[value=1.2e1]',         3",
      "false,               '12.34e-34 ', true,      'Value[value=1.234e1]',       5",
      "false,               ' 12e',       true,      'Value[value=1.2e1]',         3",
      "false,               '.',          false,     ,                             1",
      "false,               '.e',         false,     ,                             1",
      "false,               'e1',         false,     ,                             0",
  })
  public void valueParser(boolean allowExponent, String input, boolean isSuccess, String token,
      int position) {
    Parser parser = ParserFactory.valueParser(allowExponent, 10);

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Value) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // input,        isSuccess, token,                                                position
      "  ' + 1',       true,      'TstExpr[op=plus, args=[Value[value=1]]]',     4",
      "  '-2',         true,      'TstExpr[op=minus, args=[Value[value=2]]]',    2",
      "  '1',          false,     ,                                                     0",
      "  '-',          false,     ,                                                     1",
  })
  public void unaryOperatorParser(String input, boolean isSuccess, String token, int position) {
    Parser parser = ParserFactory.unaryOperatorParser(
        ParserFactory.valueParser(false, 10),
        Map.of(
            '+', e -> new TstExpr("plus", List.of(e)),
            '-', e -> new TstExpr("minus", List.of(e)))
    );

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // input,        isSuccess, token, position
      "  ' 1 + 2 ',    true,      'TstExpr[op=plus, args=[Value[value=1], Value[value=2]]]', 7",
      "  '1+2-3+4',    true,      'TstExpr[op=plus, args=[TstExpr[op=minus, args=[TstExpr[op=plus, args=[Value[value=1], Value[value=2]]], Value[value=3]]], Value[value=4]]]', 7",
      "  '1+2*3',      true,      'TstExpr[op=plus, args=[Value[value=1], Value[value=2]]]', 3",
      "  '1',          false,     ,                                                          1",
      "  '-',          false,     ,                                                          0",
  })
  public void binaryLeftAssocOperatorParser(String input, boolean isSuccess, String token,
      int position) {
    Parser parser = ParserFactory.binaryLeftAssocOperatorParser(
        ParserFactory.valueParser(false, 10),
        ParserFactory.valueParser(false, 10),
        Map.of(
            '+', (e, f) -> new TstExpr("plus", List.of(e, f)),
            '-', (e, f) -> new TstExpr("minus", List.of(e, f)))
    );

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // input,        isSuccess, token, position
      "  ' 1 + 2 ',    true,      'TstExpr[op=plus, args=[Value[value=1], Value[value=2]]]', 7",
      "  '1+2-3+4',    true,      'TstExpr[op=plus, args=[Value[value=1], TstExpr[op=minus, args=[Value[value=2], TstExpr[op=plus, args=[Value[value=3], Value[value=4]]]]]]]', 7",
      "  '1+2*3',      true,      'TstExpr[op=plus, args=[Value[value=1], Value[value=2]]]', 3",
      "  '1',          false,     ,                                                          1",
      "  '-',          false,     ,                                                          0",
  })
  public void binaryRightAssocOperatorParser(String input, boolean isSuccess, String token,
      int position) {
    Parser parser = ParserFactory.binaryRightAssocOperatorParser(
        ParserFactory.valueParser(false, 10),
        ParserFactory.valueParser(false, 10),
        Map.of(
            '+', (e, f) -> new TstExpr("plus", List.of(e, f)),
            '-', (e, f) -> new TstExpr("minus", List.of(e, f)))
    );

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // separator, tailRequired,  input,        isSuccess, token,          position
      "  ,          false,         'a',          true,      '[a]',          1",
      "  ,          true,          'a',          false,     ,               1",
      "  '-',       false,         'a-x - y-z',  true,      '[a, x, y, z]', 9",
      "  '-',       true,         'a-x - y-z',   true,      '[a, x, y, z]', 9",
      "  ,          false,        'axyz',        true,      '[a, x, y, z]', 4",
      "  ,          true,         'axyz',        true,      '[a, x, y, z]', 4",
  })
  public void listParser(Character separator, boolean tailRequired, String input, boolean isSuccess,
      String token,
      int position) {
    Parser parser = ParserFactory.listParser(separator,
        ParserFactory.nameParser(Set.of("a", "b", "c"), "head"),
        ParserFactory.nameParser(Set.of("x", "y", "z"), "tail"),
        tailRequired);

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Object) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // input,            isSuccess, token, position
      "  ' one ( 1 ) ',    true,      'FunctionCall[name=one, arguments=[Value[value=1]]]', 11",
      "  'two(1,2)',       true,      'FunctionCall[name=two, arguments=[Value[value=1], Value[value=2]]]', 8",
      "  ' o ',            true,      'ValueName[name=o]', 3",
      "  'twov',           true,      'ValueName[name=twov]', 4",
      "  'unknown',        false,     , 0",
      "  'unknown(1,2)',   false,     , 0",
      "  'one(1,2)',       false,     , 8",
      "  'two(1)',         false,     , 6",
  })
  public void definitionParser(String input, boolean isSuccess, String token, int position) {
    ApcomplexContext context = new ApcomplexContext(100)
        .variable("o")
        .variable("twov")
        .function(new TstFn("one", 1, 1))
        .function(new TstFn("two", 2, 2));
    Parser parser = ParserFactory.definitionParser(context, ParserFactory.valueParser(false, 10));

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(isSuccess);
    if (isSuccess) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
    assertThat(result.getPosition()).isEqualTo(position);
  }

  @ParameterizedTest
  @CsvSource({
      // Primitives
      "'1.2',                 'Value[value=1.2]'",
      "'1.2e3',               'Value[value=1.2e3]'",
      "'x',                   'ValueName[name=x]'",
      "'xy',                  ",
      "'z',                   ",
      "'(x)',                 'ValueName[name=x]'",
      "'(1+x)',               'Add[left=Value[value=1], right=ValueName[name=x]]'",
      "'-x',                  'Neg[arg=ValueName[name=x]]'",
      "'fna(x)',              'FunctionCall[name=fna, arguments=[ValueName[name=x]]]'",
      "'fna(x,1)',            ",
      "'fnb(x,1)',            'FunctionCall[name=fnb, arguments=[ValueName[name=x], Value[value=1]]]'",
      "'fnc(x,1)',            ",
      // Pow
      "'2^-3^x',              'Pow[left=Value[value=2], right=Pow[left=Neg[arg=Value[value=3]], right=ValueName[name=x]]]'",
      "'-2^3^x',              'Pow[left=Neg[arg=Value[value=2]], right=Pow[left=Value[value=3], right=ValueName[name=x]]]'",
      // Factors
      "'(x+y)*2^4/fna(y)',    'Div[left=Mul[left=Add[left=ValueName[name=x], right=ValueName[name=y]], right=Pow[left=Value[value=2], right=Value[value=4]]], right=FunctionCall[name=fna, arguments=[ValueName[name=y]]]]'",
      // Sums
      "'-3+x*y-5',             'Sub[left=Add[left=Neg[arg=Value[value=3]], right=Mul[left=ValueName[name=x], right=ValueName[name=y]]], right=Value[value=5]]'",
      "'(-3+x)*y-5',           'Sub[left=Mul[left=Add[left=Neg[arg=Value[value=3]], right=ValueName[name=x]], right=ValueName[name=y]], right=Value[value=5]]'",
  })
  public void expressionParserStandard(String input, String token) {
    ApcomplexContext context = new ApcomplexContext(100)
        .function(new TstFn("fna", 1, 1))
        .function(new TstFn("fnb", 2, 2))
        .variable("x")
        .variable("y");
    Parser parser = ParserFactory.expressionParserStandard(context).end();

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(token != null);
    if (token != null) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
  }

  @ParameterizedTest
  @CsvSource({
      // Primitives
      "'1.2',                 'Value[value=1.2]'",
      "'1.2e3',               ",
      "'x',                   'ValueName[name=x]'",
      "'z',                   ",
      "'(x)',                 'ValueName[name=x]'",
      "'(1+x)',               'Add[left=Value[value=1], right=ValueName[name=x]]'",
      "'-x',                  'Neg[arg=ValueName[name=x]]'",
      "'fna(x)',              'FunctionCall[name=fna, arguments=[ValueName[name=x]]]'",
      "'fna(x,1)',            ",
      "'fnb(x,1)',            'FunctionCall[name=fnb, arguments=[ValueName[name=x], Value[value=1]]]'",
      "'fnc(x,1)',            ",
      // ConvenienceMultiplication
      "'xy',                  'Mul[left=ValueName[name=x], right=ValueName[name=y]]'",
      "'2x',                  'Mul[left=Value[value=2], right=ValueName[name=x]]'",
      "'-2x',                 'Neg[arg=Mul[left=Value[value=2], right=ValueName[name=x]]]'",
      "'2-x',                 'Sub[left=Value[value=2], right=ValueName[name=x]]'",
      "'xy-2y',               'Sub[left=Mul[left=ValueName[name=x], right=ValueName[name=y]], right=Mul[left=Value[value=2], right=ValueName[name=y]]]'",
      // Pow
      "'y^x',                 'Pow[left=ValueName[name=y], right=ValueName[name=x]]'",
      "'-y^-x',               'Neg[arg=Pow[left=ValueName[name=y], right=Neg[arg=ValueName[name=x]]]]'",
      "'2^-3^x',              'Pow[left=Value[value=2], right=Neg[arg=Pow[left=Value[value=3], right=ValueName[name=x]]]]'",
      "'-2^3^-x',             'Neg[arg=Pow[left=Value[value=2], right=Pow[left=Value[value=3], right=Neg[arg=ValueName[name=x]]]]]'",
      "'xy^-2x',              'Mul[left=ValueName[name=x], right=Pow[left=ValueName[name=y], right=Neg[arg=Mul[left=Value[value=2], right=ValueName[name=x]]]]]'",
      "'xy^-2x^3',            'Mul[left=ValueName[name=x], right=Pow[left=ValueName[name=y], right=Neg[arg=Mul[left=Value[value=2], right=Pow[left=ValueName[name=x], right=Value[value=3]]]]]]'",
      // Factors
      "'2(x+1)(x-1)^4fna(y)', 'Mul[left=Mul[left=Value[value=2], right=Add[left=ValueName[name=x], right=Value[value=1]]], right=Pow[left=Sub[left=ValueName[name=x], right=Value[value=1]], right=Mul[left=Value[value=4], right=FunctionCall[name=fna, arguments=[ValueName[name=y]]]]]]'",
      // Sums
      "'-3+x*y-5',            'Sub[left=Add[left=Neg[arg=Value[value=3]], right=Mul[left=ValueName[name=x], right=ValueName[name=y]]], right=Value[value=5]]'",
      "'(-3+x)*y-5',          'Sub[left=Mul[left=Add[left=Neg[arg=Value[value=3]], right=ValueName[name=x]], right=ValueName[name=y]], right=Value[value=5]]'",
  })
  public void expressionParserSimplified(String input, String token) {
    ApcomplexContext context = new ApcomplexContext(100)
        .function(new TstFn("fna", 1, 1))
        .function(new TstFn("fnb", 2, 2))
        .variable("x")
        .variable("y");
    Parser parser = ParserFactory.expressionParserSimplified(context)
        .end(); //Tracer.on(ParserFactory.expressionParserSimplified(context).end(), evt -> System.err.println(evt));

    Result result = parser.parse(input);
    assertThat(result.isSuccess()).isEqualTo(token != null);
    if (token != null) {
      assertThat(String.valueOf((Expression) result.get())).isEqualTo(token);
    }
  }


  private record TstExpr(String op, List<Expression> args) implements Expression {

    @Override
    public boolean isInvariant(Context<?, ?> context) {
      return false;
    }

    @Override
    public Expression simplify(Context<?, ?> context) {
      return null;
    }

    @Override
    public Apcomplex evaluate(Context<?, ?> context) {
      return null;
    }
  }

  private record TstFn(String name, int minArgs, int maxArgs) implements FunctionDefinition {

    @Override
    public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
      return null;
    }
  }
}
