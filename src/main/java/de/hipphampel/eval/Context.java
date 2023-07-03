package de.hipphampel.eval;

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

import de.hipphampel.eval.definition.Constant;
import de.hipphampel.eval.definition.Definition;
import de.hipphampel.eval.definition.ExpressionFunction;
import de.hipphampel.eval.definition.FunctionDefinition;
import de.hipphampel.eval.definition.StandardFunctions;
import de.hipphampel.eval.definition.ValueDefinition;
import de.hipphampel.eval.definition.Variable;
import de.hipphampel.eval.exception.EvalException;
import de.hipphampel.eval.exception.ParseException;
import de.hipphampel.eval.expr.Expression;
import de.hipphampel.eval.parser.MacroExpander;
import de.hipphampel.eval.parser.ParseMode;
import de.hipphampel.eval.parser.ParserFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apfloat.Apcomplex;
import org.apfloat.FixedPrecisionApcomplexHelper;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;

/**
 * Context for evaluating math expression.
 * <p>
 * The basic aim of this class is to evaulate mathematical expressions like {@code "2+3"}, or
 * {@code "17*x-3*sin(y)"}. In the most simple case, these expressions are passed as string literals
 * to the {@link #evaluate(String) evaluate} method.
 * <p>
 * As the examples above suggest, a {@code Context} is also able to evaluate expressions that
 * contain function calls or refer to variables or constants. So beside the core evaluation
 * functionality the {@code Context} also acts as a container for {@link Definition Definitions},
 * which might be functions, constants, or variables.
 * <p>
 * Internally, all computations are done based on the {@link Apcomplex} class, which allows to deal
 * with complex number having an arbitrary precision. Concrete subclasses of this have to implement
 * some methods to translate this complex into their own representation. For example, The
 * {@link DoubleContext} implementation translates the number from and to {@code doubles}.
 * <p>
 * Most aspects of this class can be freely configured, such as which constants, variable are known
 * and how to parse expression literals. The only exception is the {@link #precision() precision},
 * which must be specified at construction time and cannot be changed after construction.
 *
 * @param <V> The type of the value
 * @param <C> The type of the specialisation of this class (technically motivated)
 * @see DoubleContext
 * @see BigDecimalContext
 * @see ApfloatContext
 * @see ApcomplexContext
 */
public abstract class Context<V, C extends Context<V, C>> {

  private static final String NAME_PATTERN_STR = "^[a-zA-Z]+$";
  private static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STR);
  private final FixedPrecisionApcomplexHelper precisionHelper;

  private ParseMode parseMode;
  private MacroExpander macroExpander;
  private final Map<String, Definition> definitions;

  /**
   * Constructor
   *
   * @param precision The precision
   */
  protected Context(long precision) {
    this(new FixedPrecisionApcomplexHelper(precision));
  }

  /**
   * Constructor
   *
   * @param precisionHelper The {@link FixedPrecisionApcomplexHelper}
   */
  protected Context(FixedPrecisionApcomplexHelper precisionHelper) {
    this.precisionHelper = Objects.requireNonNull(precisionHelper);
    this.parseMode = ParseMode.STANDARD;
    this.macroExpander = MacroExpander.NOP;
    this.definitions = new HashMap<>();
  }

  /**
   * Gets the associated {@link FixedPrecisionApcomplexHelper}
   *
   * @return The precision helper.
   */
  public FixedPrecisionApcomplexHelper precisionHelper() {
    return precisionHelper;
  }

  /**
   * Gets the precision.
   * <p>
   * It defines how precise the calculation are done. The value cannot be changed after
   * construction.
   *
   * @return The precision of numbers.
   */
  public long precision() {
    return precisionHelper.precision();
  }

  /**
   * Gets the mode how to parse expressions.
   * <p>
   * Please refer to the {@link ParserFactory} for details.
   *
   * @return The {@link ParseMode}
   */
  public ParseMode parseMode() {
    return parseMode;
  }

  /**
   * Sets the mode  how to parse expressions.
   * <p>
   * Please refer to the {@link ParserFactory} for details.
   *
   * @param parseMode The {@link ParseMode}
   * @return This instance
   */
  public C parseMode(ParseMode parseMode) {
    if (this.parseMode == parseMode) {
      return self();
    }
    this.parseMode = Objects.requireNonNull(parseMode);
    return self();
  }

  /**
   * Gets the currently configured {@link MacroExpander}.
   * <p>
   * Please refer to {@link MacroExpander} for details
   *
   * @return The {@code MacroExpander}
   */
  public MacroExpander macroExpander() {
    return this.macroExpander;
  }

  /**
   * Sets the current {@link MacroExpander}.
   * <p>
   * Please refer to {@link MacroExpander} for details
   *
   * @param macroExpander The new {@code MacroExpander}
   * @return This instance.
   */
  public C macroExpander(MacroExpander macroExpander) {
    this.macroExpander = macroExpander == null ? MacroExpander.NOP : macroExpander;
    return self();
  }

  /**
   * Creates a deep copy of this instance.
   * <p>
   * The copy has the same settings, function and constant definitions. Depending on the value of
   * {@code withVariables}, it also has the same variables.
   *
   * @param withVariables {@code true}, if copy the variables as well.
   * @return The copy
   * @see #copyAsApcomplexContext(boolean)
   */
  public C copy(boolean withVariables) {
    Context<V, C> copy = newInstance();
    fillContext(copy, withVariables);
    return copy.self();
  }

  /**
   * Creates a copy of this instance as a {@link ApcomplexContext}.
   * <p>
   * This method works similar to the {@link #copy(boolean)}, but creates a {@code ApcomplexContext}
   * instead of the original type; the copy has the same settings, function and constant
   * definitions. Depending on the value of {@code withVariables}, it also has the same variables.
   *
   * @param withVariables {@code true}, if copy the variables as well.
   * @return The copy
   * @see #copy(boolean)
   */
  public ApcomplexContext copyAsApcomplexContext(boolean withVariables) {
    ApcomplexContext copy = new ApcomplexContext(precisionHelper);
    fillContext(copy, withVariables);
    return copy;
  }

  private void fillContext(Context<?, ?> copy, boolean withVariables) {
    copy.parseMode(this.parseMode);
    definitions(Definition.class)
        .filter(def -> withVariables || !(def instanceof Variable))
        .forEach(copy::define);
  }

  /**
   * Evaluates the given {@code expression}.
   * <p>
   * It basically parses {@code expression} using the a parser that matches the {@link #parseMode()}
   * and evaluates it then.
   *
   * @param expression The expression string
   * @return The result
   * @throws EvalException On any kind of error
   */
  public V evaluate(String expression) {
    return evaluate(parse(expression));
  }

  /**
   * Evaluates the given {@code expression}.
   * <p>
   * The expression should be parsed by this context via the {@link #parse(String)} method.
   *
   * @param expression The exopression
   * @return The result
   * @throws EvalException On any kind of error
   */
  public V evaluate(Expression expression) {
    return fromApcomplex(expression.evaluate(this));
  }

  /**
   * Parses the string literal {@code expression} into a {@link Expression},
   * <p>
   * The returned {@code Expression} can be used in {@link #evaluate(Expression)}
   *
   * @param expression The literal to parse.
   * @return The {@code Expression}
   * @throws ParseException If parsing fails
   */
  public Expression parse(String expression) {
    String expanded = macroExpander.expand(this, expression);
    Result result = newExpressionParser().parse(expanded);
    if (result.isFailure()) {
      throw new ParseException(result.getMessage(), result.getPosition());
    }
    return result.get();
  }

  /**
   * Adds the given {@link Definition}.
   * <p>
   * If a same named {@code Definition} already exists, an exception is thrown. This method is
   * protected, since normally {@code Definitions} are added via specialized methods.
   *
   * @param definition Definition to add.
   * @param <T>        The type of the {@code Definition}
   * @return The definition
   * @throws EvalException If trying to redefine an existing {@code Definition}
   */
  protected <T extends Definition> T define(T definition) {
    String name = definition.name();
    validateName(name);
    Definition existing = definitions.get(name);
    if (existing != null) {
      throw new EvalException(
          "Redefinition of '" + name + "' (there is already a " + definitionTypeToString(
              existing.getClass()) + " with this name)");
    }
    definitions.put(definition.name(), definition);
    return definition;
  }

  /**
   * Validates the given {@code name} whether its syntax is correct.
   * <p>
   * This default implementation requires that all characters of the name are alphabetic. When
   * overriding this method bear bin mind that the names should be parsable somehow; i.e they shouöd
   * not start or end with digits or dots or should not contain spaces.
   *
   * @param name Name to validate
   * @throws EvalException If name is not valid
   */
  protected void validateName(String name) {
    if (!NAME_PATTERN.matcher(name).matches()) {
      throw new EvalException(
          "Invalid name '" + name + "' - must conform to pattern '" + NAME_PATTERN_STR + "'");
    }
  }

  /**
   * Returns a Stream with all {@link Definition Definitions} having the given type.
   * <p>
   * The method is protected, since normally {@code Definitions} are accessed via specialized
   * methods.
   *
   * @param type The type of the {@code Definitions}
   * @param <T>  The type of the {@code Definitions}
   * @return A {@code Stream} with the matching {@code Definitions}
   */
  @SuppressWarnings("unchecked")
  protected <T extends Definition> Stream<T> definitions(Class<T> type) {
    return definitions.values().stream()
        .filter(def -> type.isAssignableFrom(def.getClass()))
        .map(def -> (T) def);
  }

  /**
   * Gets the definition named {@code name} having the type {@code type}.
   * <p>
   * The method fails, if there is a {@link Definition} with the wrong type. It is protected, since
   * normally {@code Definitions} are accessed via specialized methods.
   *
   * @param name Name of the {@code Definition}
   * @param type The type of the {@code Definition}
   * @param <T>  The type of the {@code Definition}
   * @return The {@code Definition}
   * @throws EvalException If the {@code Definition} does not exist.
   */
  protected <T extends Definition> Optional<T> definition(String name, Class<T> type) {
    Definition definition = definitions.get(name);
    if (definition == null) {
      return Optional.empty();
    }
    if (!type.isAssignableFrom(definition.getClass())) {
      throw new EvalException(
          "'" + name + "' is a " + definitionTypeToString(definition.getClass()) + " but not a "
              + definitionTypeToString(type));
    }
    return Optional.of(type.cast(definition));
  }

  /**
   * Internal helper to translate a type name to a string.
   * <p>
   * This method is used to form exception messages
   *
   * @param type The type
   * @return A string describing the type
   */
  protected String definitionTypeToString(Class<? extends Definition> type) {
    if (FunctionDefinition.class.isAssignableFrom(type)) {
      return "function";
    } else if (Variable.class.isAssignableFrom(type)) {
      return "variable";
    } else if (Constant.class.isAssignableFrom(type)) {
      return "constant";
    } else if (ValueDefinition.class.isAssignableFrom(type)) {
      return "value";
    }
    return "definition";
  }

  /**
   * Defines a constant.
   * <p>
   * A constant is a named value, whereas the value is immutable. Once created, a constant cannot be
   * removed or changed.
   * <p>
   * To retrieve the value of the constant once defined, use {@link #value(String)}
   *
   * @param name  Name of the constant
   * @param value Value of the constant
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a function, constant, or variable
   *                       with the same name already exists
   * @see #value(String)
   */
  public C constant(String name, V value) {
    return constants(new Constant(name, toApcomplex(value)));
  }

  /**
   * Defines the given constants.
   * <p>
   * A constant is a named value, whereas the value is immutable. Once created, a constant cannot be
   * removed or changed.
   * <p>
   * To retrieve the value of the constant once defined, use {@link #value(String)}
   *
   * @param constants The constant definitions.
   * @return This instance
   */
  public C constants(Constant... constants) {
    return constants(Arrays.asList(constants));
  }

  /**
   * Defines the given constants.
   * <p>
   * A constant is a named value, whereas the value is immutable. Once created, a constant cannot be
   * removed or changed.
   * <p>
   * To retrieve the value of the constant once defined, use {@link #value(String)}
   *
   * @param constants The constant definitions.
   * @return This instance
   */
  public C constants(Collection<Constant> constants) {
    constants.forEach(this::define);
    return self();
  }

  /**
   * Defines the given constants.
   * <p>
   * This is basically a shortcut for calling {@link #constant(String, Object)} for each entry og
   * the {@code constants} map.
   *
   * @param constants The constants to define
   * @return This instance
   */
  public C constants(Map<String, V> constants) {
    constants.forEach(this::constant);
    return self();
  }

  /**
   * Returns a map with all constants and their values.
   * <p>
   * The returned map is not bound to this instance, so that modifications on it do not affect this
   * context. Note that this method returns only constants, to get the variables use
   * {@link #variables()} to get all variables or {@link #values()} to get all constants and values
   *
   * @return The map with the constants
   * @see #variables()
   * @see #values()
   */
  public Map<String, V> constants() {
    return definitions(Constant.class)
        .collect(Collectors.toMap(
            Constant::name,
            def -> fromApcomplex(def.value(this))
        ));
  }

  /**
   * Defines a variable unless it already exists.
   * <p>
   * The initial value of the variable is {@code 0}. Variables are named values. A variable cannot
   * be removed, but its value can be changed. The method does nothing in case the variable already
   * exists.
   * <p>
   * To get the current value of a variable use {@link #value(String)}; to change it use
   * {@link #variable(String, Object)}.
   *
   * @param name The name of the variable
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a functio or constant with the same
   *                       name already exists
   * @see #variables(Collection)
   * @see #value(String)
   * @see #variable(String, Object)
   */
  public C variable(String name) {
    definition(name, Variable.class).ifPresentOrElse(
        var -> var.value(this, Apcomplex.ZERO),
        () -> define(new Variable(name))
    );
    return self();
  }

  /**
   * Defines the given variables unless they already exists.
   * <p>
   * Technically, this is a shortcut for calling {@link #variable(String)} for each {@code name}
   *
   * @param first   The first variable name
   * @param further Further variable names
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a functio or constant with the same
   *                       name already exists
   * @see #variable(String)
   */
  public C variables(String first, String... further) {
    variable(first);
    return variables(Arrays.asList(further));
  }

  /**
   * Defines the given variables unless they already exists.
   * <p>
   * Technically, this is a shortcut for calling {@link #variable(String)} for each {@code name}
   *
   * @param names The variable names
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a functio or constant with the same
   *                       name already exists
   * @see #variable(String)
   */
  public C variables(Collection<String> names) {
    names.forEach(this::variable);
    return self();
  }

  /**
   * Defines a variable named {@code name} or sets it to the given {@code value}.
   * <p>
   * Variables are named values. A variable cannot be removed, but its value can be changed. This
   * version of the method allows to overwrite the value of variable. If the variable does not exist
   * yet, it is created.
   *
   * @param name  The name of the variable
   * @param value The new variable value
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a function or constant with the same
   *                       name already exists
   */
  public C variable(String name, V value) {

    definition(name, Variable.class).orElseGet(() -> define(new Variable(name)))
        .value(this, toApcomplex(value));
    return self();
  }

  /**
   * Defines or sets the given variables.
   * <p>
   * This is technically a shortcut for calling {@link #variable(String, Object)} for each member of
   * {@code variables}
   *
   * @param variables The map of the variables to set
   * @return This instance
   * @throws EvalException If the {@code name} is not valid or a function or constant with the same
   *                       name already exists
   */
  public C variables(Map<String, V> variables) {
    variables.forEach(this::variable);
    return self();
  }

  /**
   * Returns a map with all variables and their current values.
   * <p>
   * The returned map is not bound to this instance, so that modifications on it do not affect this
   * context
   *
   * @return The map with the constants
   */
  public Map<String, V> variables() {
    return definitions(Variable.class)
        .collect(Collectors.toMap(
            Variable::name,
            def -> fromApcomplex(def.value(this))
        ));
  }

  /**
   * Gets the value of the given constant or variable.
   *
   * @param name Name of the constant or variable
   * @return The value.
   * @throws EvalException if there is no such variable or constant
   */
  public V value(String name) {
    return fromApcomplex(valueAsApcomplex(name));
  }

  /**
   * Gets a map with all defined constants and variables.
   *
   * @return A map with all variables and constants.
   */
  public Map<String, V> values() {
    return definitions(ValueDefinition.class)
        .collect(Collectors.toMap(
            ValueDefinition::name,
            def -> fromApcomplex(def.value(this))
        ));
  }

  /**
   * Gets the names of all known constants and variables
   *
   * @return The constant and variable names.
   */
  public Set<String> valueNames() {
    return definitions(ValueDefinition.class).map(ValueDefinition::name)
        .collect(Collectors.toSet());
  }

  /**
   * Gets the value of the given constant or variable as a {@link Apcomplex}.
   * <p>
   * The method throws an exception in case that {@code name} does not exist.
   *
   * @param name The name of the constant or variable
   * @return The value as {@code Apcomplex}
   * @throws EvalException if there is no such variable or constant
   */
  public Apcomplex valueAsApcomplex(String name) {
    ValueDefinition def = definition(name, ValueDefinition.class).orElseThrow(
        () -> new EvalException("No such value '" + name + "'"));
    return def.value(this);
  }

  /**
   * Checks, whether {@code name} refers to a constant.
   *
   * @param name The name to check.
   * @return {@code true}, if {@code name} is a known constant
   */
  public boolean isConstant(String name) {
    return definition(name, Definition.class)
        .map(def -> def instanceof Constant)
        .orElse(false);
  }

  /**
   * Defines the given function.
   * <p>
   * The method fails, if the name of the function is already in use for a different definition.
   *
   * @param definition The {@code FunctionDefinition}
   * @return This instance
   * @throws EvalException If the function cannot be defined.
   */
  public C function(FunctionDefinition definition) {
    return functions(definition);
  }

  /**
   * Defines the given functions.
   * <p>
   * The method fails, if the name of one of the function is already in use for a different
   * definition.
   *
   * @param definitions The {@code FunctionDefinitions}
   * @return This instance
   * @throws EvalException If the function cannot be defined.
   */
  public C functions(FunctionDefinition... definitions) {
    return functions(Arrays.asList(definitions));
  }

  /**
   * Defines the given functions.
   * <p>
   * The method fails, if the name of one of the function is already in use for a different
   * definition.
   *
   * @param definitions The {@code FunctionDefinitions}
   * @return This instance
   * @throws EvalException If the function cannot be defined.
   */
  public C functions(Collection<? extends FunctionDefinition> definitions) {
    definitions.forEach(this::define);
    return self();
  }

  /**
   * Defines a function named {@code name}, having the given {@code parameters} and
   * {@code definition}.
   * <p>
   * Technically, this method creates a {@link ExpressionFunction} based on the given arguments. The
   * {@code definition} is parsed using the current {@link #parseMode()}.
   * <p>
   * The {@code definition} must refer only to constants and the {@code parameters}, nt to
   * variables. It is allowed to call other functions defined fot this context.
   *
   * @param name       The name of the function
   * @param parameters List of parameter names
   * @param definition The Expression literal
   * @return This instance
   * @throws EvalException If the function cannot be defined.
   */
  public C function(String name, List<String> parameters, String definition) {
    return functions(new ExpressionFunction(name, this, parameters, definition));
  }

  /**
   * Gets the function with the given {@code name}.
   * <p>
   * The method fails, if no such function exists.
   *
   * @param name Name of the function
   * @return The {@link FunctionDefinition}
   * @throws EvalException If the function cannot be found.
   */
  public FunctionDefinition function(String name) {
    return definition(name, FunctionDefinition.class)
        .orElseThrow(() -> new EvalException("No such function '" + name + "'"));
  }

  /**
   * Gets the names of all defined functions.
   *
   * @return The function names.
   */
  public Set<String> functionNames() {
    return definitions(FunctionDefinition.class).map(FunctionDefinition::name)
        .collect(Collectors.toSet());
  }

  /**
   * Defines the trigonometric functions {@code sin}, {@code cos}, and {@code tan}.
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withTrigonometricFunctions() {
    return functions(StandardFunctions.SIN, StandardFunctions.COS, StandardFunctions.TAN);
  }

  /**
   * Defines the inverse trigonometric functions {@code asin}, {@code acos}, and {@code atan}.
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withInverseTrigonometricFunctions() {
    return functions(StandardFunctions.ASIN, StandardFunctions.ACOS, StandardFunctions.ATAN);
  }

  /**
   * Defines the hyperbolic functions {@code sinh}, {@code cosh}, and {@code tanh}.
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withHyperbolicFunctions() {
    return functions(StandardFunctions.SINH, StandardFunctions.COSH, StandardFunctions.TANH);
  }

  /**
   * Defines the inverse hyperbolic functions {@code asinh}, {@code acosh}, and {@code atanh}.
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withInverseHyperbolicFunctions() {
    return functions(StandardFunctions.ASINH, StandardFunctions.ACOSH, StandardFunctions.ATANH);
  }

  /**
   * Defines the logarithm functions {@code ln} (for base e), {@code log} (for any base).
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withLogarithmFunctions() {
    return functions(StandardFunctions.LN, StandardFunctions.LOG);
  }

  /**
   * Defines the complex specific functions {@code real}, {@code imag}, {@code norm}, {@code arg},
   * and {@code abs}.
   * <p>
   * This is basically a shortcut for calling {@link #functions(FunctionDefinition...)} for each of
   * the functions mentioned above.
   * <p>
   * These functions are defined for real and complex numbers.
   *
   * @return This instance
   */
  public C withComplexSpecificFunctions() {
    return functions(StandardFunctions.ARG, StandardFunctions.ABS, StandardFunctions.NORM,
        StandardFunctions.IMAG, StandardFunctions.REAL);
  }

  /**
   * Converts the {@code value} from the value type of this instance to a {@link Apcomplex}.
   *
   * @param value The value to convert
   * @return The converted value
   */
  protected abstract Apcomplex toApcomplex(V value);

  /**
   * Converts the {@link Apcomplex} {@code value} into the value type of this instance
   *
   * @param value Value to convert
   * @return Converted value
   */
  protected abstract V fromApcomplex(Apcomplex value);

  /**
   * Creates a new, empty instance having the same {@code precision}.
   *
   * @return The new {@code Context}
   */
  protected abstract C newInstance();

  /**
   * Returns this instance
   *
   * @return This instance
   */
  @SuppressWarnings("unchecked")
  protected C self() {
    return (C) this;
  }

  private Parser newExpressionParser() {
    return switch (parseMode) {
      case STANDARD -> ParserFactory.expressionParserStandard(this).end();
      case SIMPLIFIED -> ParserFactory.expressionParserSimplified(this).end();
    };
  }

}
