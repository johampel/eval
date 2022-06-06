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

import de.hipphampel.eval.definition.StandardConstants;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;

/**
 * {@link Context} implementation that deals with {@code doubles}.
 * <p>
 * This implementation has a fixed precision of {@code 20}, which is a little biut higher than the
 * normal precision of a {@code double} to prevent/reduce rounding errors.
 * <p>
 * It inherits all its concepts from the base class, see {@linkplain Context there} for more
 * information.
 *
 * @see Context
 */
public class DoubleContext extends Context<Double, DoubleContext> {

  /**
   * Default constructor
   */
  public DoubleContext() {
    super(20); // A double has a precision of 16, we add 4 to have minor rounding errors
  }

  /**
   * Creates a minimal instance.
   * <p>
   * A minimal instance contains no variable, constant or function definition.
   *
   * @return The new instance
   */
  public static DoubleContext minimal() {
    return new DoubleContext();
  }

  /**
   * Creates a standard instance.
   * <p>
   * A standard instance provides the constants {@code pi} ({@code 3.14...}) and {@code e}
   * ({@code 2.71...} as well as the following commonly used functions: {@code sin}, {@code cos},
   * {@code tan}, {@code asin}, {@code acos}, {@code atan}, {@code sinh}, {@code cosh},
   * {@code tanh}, {@code asinh}, {@code acosh}, {@code atanh}, {@code log}, {@code ln}
   *
   * @return The new instance
   */
  public static DoubleContext standard() {
    return minimal()
        .constants(StandardConstants.PI(), StandardConstants.E())
        .withHyperbolicFunctions()
        .withInverseHyperbolicFunctions()
        .withTrigonometricFunctions()
        .withInverseTrigonometricFunctions()
        .withLogarithmFunctions();
  }

  @Override
  protected Apcomplex toApcomplex(Double value) {
    return new Apcomplex(new Apfloat(value, precision()));
  }

  @Override
  protected Double fromApcomplex(Apcomplex value) {
    if (!value.imag().equals(Apfloat.ZERO)) {
      return Double.NaN;
    }
    return Double.valueOf(value.real().toString());
  }

  @Override
  protected DoubleContext newInstance() {
    return new DoubleContext();
  }
}
