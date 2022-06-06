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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import de.hipphampel.eval.parser.ParseMode;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ContextTest {

  @Test
  public void precision() {
    int precision = new Random().nextInt(100);
    TestContext context = new TestContext(precision);
    assertThat(context.precision()).isEqualTo(precision);
  }

  @Test
  public void parseMode() {
    TestContext context = new TestContext(10);
    assertThat(context.parseMode()).isEqualTo(ParseMode.STANDARD);

    assertThat(context.parseMode(ParseMode.SIMPLIFIED)).isSameAs(context);
    assertThat(context.parseMode()).isEqualTo(ParseMode.SIMPLIFIED);

    assertThat(context.parseMode(ParseMode.STANDARD)).isSameAs(context);
    assertThat(context.parseMode()).isEqualTo(ParseMode.STANDARD);
  }

  @Test
  public void copy_withVariables() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.values()).isEqualTo(Map.of("aVar", 2L, "aConst", 3L));
    assertThat(context.function("aFunction")).isNotNull();

    TestContext copy = context.copy(true);
    assertThat(copy).isNotSameAs(context);
    assertThat(copy.values()).isEqualTo(context.values());
    assertThat(copy.function("aFunction")).isSameAs(context.function("aFunction"));
  }

  @Test
  public void copy_withoutVariables() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.values()).isEqualTo(Map.of("aVar", 2L, "aConst", 3L));
    assertThat(context.function("aFunction")).isNotNull();

    TestContext copy = context.copy(false);
    assertThat(copy).isNotSameAs(context);
    assertThat(copy.values()).isEqualTo(Map.of("aConst", 3L));
    assertThat(copy.function("aFunction")).isSameAs(context.function("aFunction"));
  }

  @Test
  public void copyAsApcomplexContextwithVariables() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.values()).isEqualTo(Map.of("aVar", 2L, "aConst", 3L));
    assertThat(context.function("aFunction")).isNotNull();

    ApcomplexContext copy = context.copyAsApcomplexContext(true);
    assertThat(copy.values()).isEqualTo(
        Map.of("aVar", new Apcomplex("2"), "aConst", new Apcomplex("3")));
    assertThat(copy.function("aFunction")).isSameAs(context.function("aFunction"));
  }

  @Test
  public void copyAsApcomplexContext_withoutVariables() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.values()).isEqualTo(Map.of("aVar", 2L, "aConst", 3L));
    assertThat(context.function("aFunction")).isNotNull();

    ApcomplexContext copy = context.copyAsApcomplexContext(false);
    assertThat(copy.values()).isEqualTo(Map.of("aConst", new Apcomplex("3")));
    assertThat(copy.function("aFunction")).isSameAs(context.function("aFunction"));
  }

  @ParameterizedTest
  @CsvSource({
      "de.hipphampel.eval.definition.FunctionDefinition, function",
      "de.hipphampel.eval.definition.ExpressionFunction, function",
      "de.hipphampel.eval.definition.ValueDefinition,    value",
      "de.hipphampel.eval.definition.Constant,           constant",
      "de.hipphampel.eval.definition.Variable,           variable",
      "de.hipphampel.eval.definition.Definition,         definition",
  })
  public void definitionTypeToString(Class<? extends Definition> type, String name) {
    TestContext context = new TestContext(10);
    assertThat(context.definitionTypeToString(type)).isEqualTo(name);
  }

  @Test
  public void define_success() {
    TestContext context = new TestContext(10);
    assertThat(context.definitions(Definition.class).toList()).isEmpty();

    Constant constant = new Constant("one", Apcomplex.ONE);
    assertThat(context.define(constant)).isSameAs(constant);
    assertThat(context.definitions(Definition.class).toList()).containsExactlyInAnyOrder(
        constant
    );

    Variable variable = new Variable("two");
    assertThat(context.define(variable)).isSameAs(variable);
    assertThat(context.definitions(Definition.class).toList()).containsExactlyInAnyOrder(
        constant,
        variable
    );

    FunctionDefinition function = new TestFunction("three", 1, 1, Apcomplex.I);
    assertThat(context.define(function)).isSameAs(function);
    assertThat(context.definitions(Definition.class).toList()).containsExactlyInAnyOrder(
        constant,
        variable,
        function
    );
  }

  @Test
  public void define_invalidName() {
    TestContext context = new TestContext(10);

    assertThatThrownBy(() -> context.define(new Variable("bad guy")))
        .hasMessage("Invalid name 'bad guy' - must conform to pattern '^[a-zA-Z]+$'")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void define_redefinition() {
    TestContext context = new TestContext(10);
    context.define(new Variable("a"));
    context.define(new Constant("b", Apcomplex.ZERO));
    context.define(new TestFunction("c", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.define(new Variable("a")))
        .hasMessage("Redefinition of 'a' (there is already a variable with this name)")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.define(new Variable("b")))
        .hasMessage("Redefinition of 'b' (there is already a constant with this name)")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.define(new Variable("c")))
        .hasMessage("Redefinition of 'c' (there is already a function with this name)")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void definitions() {
    TestContext context = new TestContext(10);
    Variable a = context.define(new Variable("a"));
    Constant b = context.define(new Constant("b", Apcomplex.ZERO));
    FunctionDefinition c = context.define(new TestFunction("c", 1, 1, Apcomplex.ZERO));
    Variable d = context.define(new Variable("d"));
    Constant e = context.define(new Constant("e", Apcomplex.ZERO));
    FunctionDefinition f = context.define(new TestFunction("f", 1, 1, Apcomplex.ZERO));

    assertThat(context.definitions(Definition.class)).containsExactlyInAnyOrder(
        a, b, c, d, e, f
    );
    assertThat(context.definitions(FunctionDefinition.class)).containsExactlyInAnyOrder(
        c, f
    );
    assertThat(context.definitions(ValueDefinition.class)).containsExactlyInAnyOrder(
        a, b, d, e
    );
    assertThat(context.definitions(Variable.class)).containsExactlyInAnyOrder(
        a, d
    );
    assertThat(context.definitions(Constant.class)).containsExactlyInAnyOrder(
        b, e
    );
  }

  @Test
  public void definition_success() {
    TestContext context = new TestContext(10);
    Variable a = context.define(new Variable("a"));
    Constant b = context.define(new Constant("b", Apcomplex.ZERO));
    FunctionDefinition c = context.define(new TestFunction("c", 1, 1, Apcomplex.ZERO));

    assertThat(context.definition("a", Definition.class)).contains(a);
    assertThat(context.definition("a", ValueDefinition.class)).contains(a);
    assertThat(context.definition("a", Variable.class)).contains(a);

    assertThat(context.definition("b", Definition.class)).contains(b);
    assertThat(context.definition("b", ValueDefinition.class)).contains(b);
    assertThat(context.definition("b", Constant.class)).contains(b);

    assertThat(context.definition("c", Definition.class)).contains(c);
    assertThat(context.definition("c", FunctionDefinition.class)).contains(c);
  }

  @ParameterizedTest
  @CsvSource({
      "de.hipphampel.eval.definition.FunctionDefinition",
      "de.hipphampel.eval.definition.ExpressionFunction",
      "de.hipphampel.eval.definition.ValueDefinition",
      "de.hipphampel.eval.definition.Constant",
      "de.hipphampel.eval.definition.Variable",
      "de.hipphampel.eval.definition.Definition",
  })
  public void definition_missing(Class<? extends Definition> type) {
    TestContext context = new TestContext(10);

    assertThat(context.definition("a", type)).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
      "a, de.hipphampel.eval.definition.FunctionDefinition, 'a' is a variable but not a function",
      "b, de.hipphampel.eval.definition.ExpressionFunction, 'b' is a constant but not a function",
      "c, de.hipphampel.eval.definition.ValueDefinition,    'c' is a function but not a value",
      "a, de.hipphampel.eval.definition.Constant,           'a' is a variable but not a constant",
      "b, de.hipphampel.eval.definition.Variable,           'b' is a constant but not a variable",
  })
  public void definition_wrongType(String name, Class<? extends Definition> type, String message) {
    TestContext context = new TestContext(10);
    Variable a = context.define(new Variable("a"));
    Constant b = context.define(new Constant("b", Apcomplex.ZERO));
    FunctionDefinition c = context.define(new TestFunction("c", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.definition(name, type))
        .hasMessage(message)
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void constant_success() {
    TestContext context = new TestContext(10);
    assertThat(context.constants()).isEmpty();

    context.constant("one", 1L);
    assertThat(context.constants()).isEqualTo(
        Map.of("one", 1L));

    context.constant("two", 2L);
    assertThat(context.constants()).isEqualTo(
        Map.of("one", 1L, "two", 2L));

    context.constants(Map.of("three", 3L, "four", 4L));
    assertThat(context.constants()).isEqualTo(
        Map.of("one", 1L, "two", 2L, "three", 3L, "four", 4L));
  }

  @Test
  public void constant_failure() {
    TestContext context = new TestContext(10)
        .variable("aVar")
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.constant("aVar", 1L))
        .hasMessage("Redefinition of 'aVar' (there is already a variable with this name)")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.constant("aConst", 1L))
        .hasMessage("Redefinition of 'aConst' (there is already a constant with this name)")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.constant("aFunction", 1L))
        .hasMessage("Redefinition of 'aFunction' (there is already a function with this name)")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.constant("not valid", 1L))
        .hasMessage("Invalid name 'not valid' - must conform to pattern '^[a-zA-Z]+$'")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void evaluate_success() {
    TestContext context = new TestContext(100)
        .parseMode(ParseMode.SIMPLIFIED)
        .variable("x", 10L);

    assertThat(context.evaluate("x^2-1")).isEqualTo(99L);
    assertThat(context.evaluate("3(1+2)")).isEqualTo(9L);
  }

  @Test
  public void evaluate_failure() {
    TestContext context = new TestContext(100)
        .parseMode(ParseMode.STANDARD)
        .variable("x", 10L);

    // Parse error
    assertThatThrownBy(() -> context.evaluate("2x"))
        .hasMessage("end of input expected")
        .isInstanceOf(ParseException.class);

    // Evaluation error
    assertThatThrownBy(() -> context.evaluate("1/0"))
        .hasMessage("Division by zero")
        .isInstanceOf(ArithmeticException.class);
  }

  @ParameterizedTest
  @CsvSource({
      // input,    hasError, expressionOrError
      "x+1,        false,    'Add[left=ValueName[name=x], right=Value[value=1]]'",
      "x+,         true,     'end of input expected'",
  })
  public void parse(String input, boolean hasError, String expressionOrError) {
    TestContext context = new TestContext(100)
        .parseMode(ParseMode.STANDARD)
        .variable("x", 10L);

    try {
      Expression expr = context.parse(input);
      assertThat(hasError).isFalse();
      assertThat(expr.toString()).isEqualTo(expressionOrError);
    } catch (ParseException ex) {
      assertThat(hasError).isTrue();
      assertThat(ex.getMessage()).isEqualTo(expressionOrError);
    }
  }

  @Test
  public void variable_success() {
    TestContext context = new TestContext(10);
    assertThat(context.constants()).isEmpty();

    context.variable("varA");
    assertThat(context.variables()).isEqualTo(
        Map.of("varA", 0L));

    context.variables("varB", "varC");
    assertThat(context.variables()).isEqualTo(
        Map.of("varA", 0L,
            "varB", 0L,
            "varC", 0L
        ));

    context.variable("varD", 1L);
    assertThat(context.variables()).isEqualTo(
        Map.of("varA", 0L,
            "varB", 0L,
            "varC", 0L,
            "varD", 1L
        ));

    context.variables(Map.of("varE", 2L, "varF", 3L));
    assertThat(context.variables()).isEqualTo(
        Map.of("varA", 0L,
            "varB", 0L,
            "varC", 0L,
            "varD", 1L,
            "varE", 2L,
            "varF", 3L
        ));
  }

  @Test
  public void variable_failure() {
    TestContext context = new TestContext(10)
        .variable("aVar")
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.variable("aConst", 1L))
        .hasMessage("'aConst' is a constant but not a variable")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.variable("aFunction", 1L))
        .hasMessage("'aFunction' is a function but not a variable")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.variable("not valid", 1L))
        .hasMessage("Invalid name 'not valid' - must conform to pattern '^[a-zA-Z]+$'")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void value_success() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThat(context.value("aVar")).isEqualTo(2L);
    assertThat(context.value("aConst")).isEqualTo(3L);
  }

  @Test
  public void value_failure() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.value("unknown"))
        .hasMessage("No such value 'unknown'")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.value("aFunction"))
        .hasMessage("'aFunction' is a function but not a value")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void values() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.values()).isEqualTo(Map.of(
        "aVar", 2L,
        "aConst", 3L
    ));
  }

  @Test
  public void valueNames() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));
    assertThat(context.valueNames()).containsExactlyInAnyOrder("aVar", "aConst");
  }

  @Test
  public void valueAsApcomplex_success() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThat(context.valueAsApcomplex("aVar")).isEqualTo(new Apcomplex("2"));
    assertThat(context.valueAsApcomplex("aConst")).isEqualTo(new Apcomplex("3"));
  }

  @Test
  public void valueAsApcomplex_failure() {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThatThrownBy(() -> context.valueAsApcomplex("unknown"))
        .hasMessage("No such value 'unknown'")
        .isInstanceOf(EvalException.class);
    assertThatThrownBy(() -> context.valueAsApcomplex("aFunction"))
        .hasMessage("'aFunction' is a function but not a value")
        .isInstanceOf(EvalException.class);
  }

  @ParameterizedTest
  @CsvSource({
      "aVar,    false",
      "aConst,  true",
      "unknown, false"
  })
  public void isConstant(String name, boolean expected) {
    TestContext context = new TestContext(10)
        .variable("aVar", 2L)
        .constant("aConst", 3L)
        .function(new TestFunction("aFunction", 1, 1, Apcomplex.ZERO));

    assertThat(context.isConstant(name)).isEqualTo(expected);
  }

  @Test
  public void function_success() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.function("f", List.of("x"), "x^2");
    assertThat(context.functionNames()).containsExactlyInAnyOrder("f");
    assertThat(context.function("f")).isInstanceOf(ExpressionFunction.class);
  }

  @Test
  public void function_failure() {
    TestContext context = new TestContext(10)
        .variable("f");
    assertThat(context.functionNames()).isEmpty();

    assertThatThrownBy(() -> context.function("f", List.of("x"), "x^2"))
        .hasMessage("Redefinition of 'f' (there is already a variable with this name)")
        .isInstanceOf(EvalException.class);
  }

  @Test
  public void functionNames() {
    TestContext context = new TestContext(10)
        .variable("f")
        .function("g", List.of("x"), "x")
        .function("h", List.of("x"), "x");
    assertThat(context.functionNames()).containsExactlyInAnyOrder("g", "h");
  }

  @Test
  public void withTrigonometricFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withTrigonometricFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("sin", "cos", "tan");
    assertThat(context.function("sin")).isSameAs(StandardFunctions.SIN);
    assertThat(context.function("cos")).isSameAs(StandardFunctions.COS);
    assertThat(context.function("tan")).isSameAs(StandardFunctions.TAN);
  }

  @Test
  public void withInverseTrigonometricFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withInverseTrigonometricFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("asin", "acos", "atan");
    assertThat(context.function("asin")).isSameAs(StandardFunctions.ASIN);
    assertThat(context.function("acos")).isSameAs(StandardFunctions.ACOS);
    assertThat(context.function("atan")).isSameAs(StandardFunctions.ATAN);
  }

  @Test
  public void withHyperbolicFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withHyperbolicFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("sinh", "cosh", "tanh");
    assertThat(context.function("sinh")).isSameAs(StandardFunctions.SINH);
    assertThat(context.function("cosh")).isSameAs(StandardFunctions.COSH);
    assertThat(context.function("tanh")).isSameAs(StandardFunctions.TANH);
  }

  @Test
  public void withInverseHyperbolicFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withInverseHyperbolicFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("asinh", "acosh", "atanh");
    assertThat(context.function("asinh")).isSameAs(StandardFunctions.ASINH);
    assertThat(context.function("acosh")).isSameAs(StandardFunctions.ACOSH);
    assertThat(context.function("atanh")).isSameAs(StandardFunctions.ATANH);
  }

  @Test
  public void withLogarithmFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withLogarithmFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("log", "ln");
    assertThat(context.function("log")).isSameAs(StandardFunctions.LOG);
    assertThat(context.function("ln")).isSameAs(StandardFunctions.LN);
  }

  @Test
  public void withComplexSpecificFunctions() {
    TestContext context = new TestContext(10);
    assertThat(context.functionNames()).isEmpty();

    context.withComplexSpecificFunctions();

    assertThat(context.functionNames()).containsExactlyInAnyOrder("abs", "arg", "norm", "real", "imag");
    assertThat(context.function("abs")).isSameAs(StandardFunctions.ABS);
    assertThat(context.function("arg")).isSameAs(StandardFunctions.ARG);
    assertThat(context.function("norm")).isSameAs(StandardFunctions.NORM);
    assertThat(context.function("real")).isSameAs(StandardFunctions.REAL);
    assertThat(context.function("imag")).isSameAs(StandardFunctions.IMAG);
  }


  private static class TestContext extends Context<Long, TestContext> {

    public TestContext(long precision) {
      super(precision);
    }

    @Override
    public Apcomplex toApcomplex(Long value) {
      return new Apcomplex(new Apfloat(value.toString(), precision()));
    }

    @Override
    public Long fromApcomplex(Apcomplex value) {
      if (!value.imag().equals(Apfloat.ZERO)) {
        throw new EvalException("Not an integer");
      }
      return ApfloatMath.round(value.real(), 32, RoundingMode.UP).longValue();
    }

    @Override
    protected TestContext newInstance() {
      return new TestContext(precision());
    }
  }

  private record TestFunction(String name, int minArgs, int maxArgs, Apcomplex value) implements
      FunctionDefinition {

    @Override
    public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
      return value;
    }
  }


}
