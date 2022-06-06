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
 * Base class for all kind of expressions.
 * <p>
 * The most important operation on an {@code Expression} is {@link #evaluate(Context) evaluate}.
 * Independent from the {@link Context} being used, calculations are always done based on
 * {@link Apcomplex} objects.
 */
public interface Expression {

  /**
   * Returns, whether the value returned by this instance is invariant.
   * <p>
   * This method is used in combination with the {@link #simplify(Context) simplify} method: if all
   * child expressions of this one are invariant, this expression is invariant as well and can be
   * simplified.
   *
   * @param context The {@code Context}
   * @return {@code true}, if invariant.
   */
  boolean isInvariant(Context<?, ?> context);

  /**
   * Simplifies the expression.
   * <p>
   * Simplification means that all expression that are {@linkplain #isInvariant(Context) invariant}
   * are replaced with a fixed {@link Value} instance. The method works recursively, which means
   * that something like {@code (3+4)*x } will simplified to {@code 7*x} (assuming that {@code x} is
   * a variable and therefore variant).
   *
   * @param context The {@code Context}
   * @return The simplified expression.
   */
  Expression simplify(Context<?, ?> context);

  /**
   * Evaluates this experession to a {@link Apcomplex}.
   *
   * @param context The {@link Context} providing the requested precision, variables and functions.
   * @return The result
   */
  Apcomplex evaluate(Context<?, ?> context);
}
