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

import de.hipphampel.eval.Context;
import org.apfloat.Apcomplex;

/**
 * Base class for binary {@link Expression Expressions}.
 * <p>
 * Binary expressions have a {@code left} and {@code right} operand.
 */
public interface BinaryExpression extends Expression {

  /**
   * The left operand.
   *
   * @return {@link Expression}
   */
  Expression left();

  /**
   * The right operand.
   *
   * @return {@link Expression}
   */
  Expression right();

  @Override
  default boolean isInvariant(Context<?, ?> context) {
    return left().isInvariant(context) && right().isInvariant(context);
  }

  @Override
  default Apcomplex evaluate(Context<?, ?> context) {
    Apcomplex left = left().evaluate(context);
    Apcomplex right = right().evaluate(context);
    return evaluate(context, left, right);
  }

  /**
   * Evaluates the expression on already evaluated left and right operands.
   *
   * @param context The {@link  Context}
   * @param left    Evaluated left operand
   * @param right   Evaluated right operand
   * @return Result
   */
  Apcomplex evaluate(Context<?, ?> context, Apcomplex left, Apcomplex right);
}
