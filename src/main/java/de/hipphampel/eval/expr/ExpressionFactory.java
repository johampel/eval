package de.hipphampel.eval.expr;

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

import java.util.Arrays;
import java.util.List;
import org.apfloat.Apcomplex;

/**
 * Collection of factory methods to create {@link Expression Expressions}
 */
public class ExpressionFactory {

  private ExpressionFactory() {
  }

  /**
   * Factory method to create an {@link Add} expression.
   *
   * @param left  Left operand
   * @param right Right operand
   * @return {@code Expression}
   */
  public static Add add(Expression left, Expression right) {
    return new Add(left, right);
  }

  /**
   * Factory method to create a {@link Div} expression.
   *
   * @param left  Left operand
   * @param right Right operand
   * @return {@code Expression}
   */
  public static Div div(Expression left, Expression right) {
    return new Div(left, right);
  }

  /**
   * Factory method to create a {@link FunctionCall} expression.
   *
   * @param name The function name
   * @param args The arguments
   * @return {@code Expression}
   */
  public static FunctionCall fn(String name, Expression... args) {
    return fn(name, Arrays.asList(args));
  }

  /**
   * Factory method to create a {@link FunctionCall} expression.
   *
   * @param name The function name
   * @param args The arguments
   * @return {@code Expression}
   */
  public static FunctionCall fn(String name, List<? extends Expression> args) {
    return new FunctionCall(name, args);
  }

  /**
   * Factory method to create a {@link Mul} expression.
   *
   * @param left  Left operand
   * @param right Right operand
   * @return {@code Expression}
   */
  public static Mul mul(Expression left, Expression right) {
    return new Mul(left, right);
  }

  /**
   * Factory method to create a {@link Neg} expression.
   *
   * @param arg Operand
   * @return {@code Expression}
   */
  public static Neg neg(Expression arg) {
    return new Neg(arg);
  }

  /**
   * Factory method to create a {@link Sub} expression.
   *
   * @param left  Left operand
   * @param right Right operand
   * @return {@code Expression}
   */
  public static Sub sub(Expression left, Expression right) {
    return new Sub(left, right);
  }


  /**
   * Factory method to create a {@link Pow} expression.
   *
   * @param left  Left operand
   * @param right Right operand
   * @return {@code Expression}
   */
  public static Pow pow(Expression left, Expression right) {
    return new Pow(left, right);
  }

  /**
   * Factory method to create a {@link Value} expression
   *
   * @param value The value
   * @return {@code Expression}
   */
  public static Value val(Apcomplex value) {
    return new Value(value);
  }

  /**
   * Factory method to create a {@link Value} expression
   *
   * @param value The value as string
   * @return {@code Expression}
   */
  public static Value val(String value) {
    return new Value(new Apcomplex(value));
  }

  /**
   * Factory method to create a {@link ValueName} expression
   *
   * @param name The name of the variable.
   * @return {@code Expression}
   */
  public static ValueName var(String name) {
    return new ValueName(name);
  }
}
