package de.hipphampel.eval.definition;

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

import de.hipphampel.eval.Context;
import java.util.List;
import java.util.function.BiFunction;
import org.apfloat.Apcomplex;
import org.apfloat.FixedPrecisionApcomplexHelper;

/**
 * Collection of standard functions.
 */
public class StandardFunctions {

  /**
   * Sinus. Works for complex numbers.
   */
  public static final FunctionDefinition SIN = builtin("sin", FixedPrecisionApcomplexHelper::sin);

  /**
   * Cosinus. Works for complex numbers.
   */
  public static final FunctionDefinition COS = builtin("cos", FixedPrecisionApcomplexHelper::cos);

  /**
   * Tangens. Works for complex numbers.
   */
  public static final FunctionDefinition TAN = builtin("tan", FixedPrecisionApcomplexHelper::tan);

  /**
   * Arcus sinus. Works for complex numbers.
   */
  public static final FunctionDefinition ASIN = builtin("asin",
      FixedPrecisionApcomplexHelper::asin);

  /**
   * Arcus cosinus. Works for complex numbers.
   */
  public static final FunctionDefinition ACOS = builtin("acos",
      FixedPrecisionApcomplexHelper::acos);

  /**
   * Arcus tangens. Works for complex numbers.
   */
  public static final FunctionDefinition ATAN = builtin("atan",
      FixedPrecisionApcomplexHelper::atan);

  /**
   * Sinus hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition SINH = builtin("sinh",
      FixedPrecisionApcomplexHelper::sinh);

  /**
   * Cosinus hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition COSH = builtin("cosh",
      FixedPrecisionApcomplexHelper::cosh);

  /**
   * Tangens hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition TANH = builtin("tanh",
      FixedPrecisionApcomplexHelper::tanh);

  /**
   * Arcus sinus hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition ASINH = builtin("asinh",
      FixedPrecisionApcomplexHelper::asinh);

  /**
   * Arcus cosinus hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition ACOSH = builtin("acosh",
      FixedPrecisionApcomplexHelper::acosh);

  /**
   * Arcus tangens hyperbolicus. Works for complex numbers.
   */
  public static final FunctionDefinition ATANH = builtin("atanh",
      FixedPrecisionApcomplexHelper::atanh);

  /**
   * Logarithmus naturalis. Works for complex numbers.
   */
  public static final FunctionDefinition LN = builtin("ln",
      (BiFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex>) FixedPrecisionApcomplexHelper::log);

  /**
   * Logarithmus in arbitrary base. Works for complex numbers.
   */
  public static final FunctionDefinition LOG = builtin("log", (h, a, b) -> h.log(a, b));

  /**
   * Real part of a complex number.
   */
  public static final FunctionDefinition REAL = builtin("real", FixedPrecisionApcomplexHelper::real);

  /**
   * Imaginary part of a complex number.
   */
  public static final FunctionDefinition IMAG = builtin("imag", FixedPrecisionApcomplexHelper::imag);

  /**
   * Norm of a complex number.
   */
  public static final FunctionDefinition NORM = builtin("norm", FixedPrecisionApcomplexHelper::norm);

  /**
   * Arg of a complex number.
   */
  public static final FunctionDefinition ARG = builtin("arg", FixedPrecisionApcomplexHelper::arg);

  /**
   * Absolute value.
   */
  public static final FunctionDefinition ABS = builtin("abs", FixedPrecisionApcomplexHelper::abs);

  private static FunctionDefinition builtin(String name,
      BiFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex> builtin) {
    return new UnaryBultinFunction(name, builtin);
  }

  private static FunctionDefinition builtin(String name,
      TriFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex, Apcomplex> builtin) {
    return new BinaryBultinFunction(name, builtin);
  }

  @FunctionalInterface
  private interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);
  }

  private static abstract class FixedArgCountFunction implements FunctionDefinition {

    private final String name;
    private final int numArgs;

    public FixedArgCountFunction(String name, int numArgs) {
      this.name = name;
      this.numArgs = numArgs;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public int minArgs() {
      return numArgs;
    }

    @Override
    public int maxArgs() {
      return numArgs;
    }

    protected Apcomplex validateValue(Context<?, ?> context, Apcomplex value) {
      return context.precisionHelper().valueOf(value);
    }
  }

  private static class UnaryBultinFunction extends FixedArgCountFunction {

    private final BiFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex> bultin;

    UnaryBultinFunction(String name,
        BiFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex> bultin) {
      super(name, 1);
      this.bultin = bultin;
    }

    @Override
    public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
      return bultin.apply(context.precisionHelper(), validateValue(context, args.get(0)));
    }
  }

  private static class BinaryBultinFunction extends FixedArgCountFunction {

    private final TriFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex, Apcomplex> bultin;

    BinaryBultinFunction(String name,
        TriFunction<FixedPrecisionApcomplexHelper, Apcomplex, Apcomplex, Apcomplex> bultin) {
      super(name, 2);
      this.bultin = bultin;
    }

    @Override
    public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
      return bultin.apply(context.precisionHelper(), validateValue(context, args.get(0)),
          validateValue(context, args.get(1)));
    }
  }


  private StandardFunctions() {
  }

}
