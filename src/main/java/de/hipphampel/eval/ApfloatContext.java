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
import de.hipphampel.eval.exception.NotANumberException;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;


/**
 * {@link Context} implementation that deals with {@link Apfloat Apfloats}.
 * <p>
 * Upon construction, it is possible to specify two precisions:
 * <ol>
 *   <li>One precision used for internal calculation, this is identical with the {@link #precision()}.</li>
 *   <li>Another precision implictly passed via a {@link MathContext}, which is used for converting
 *   the {@code Apfloats} from or to internal representation. The precision in the {@code
 *   MathContext} should be less or equal to the precision for the internal calculation.</li>
 * </ol>
 * <p>
 * It inherits all its concepts from the base class, see {@linkplain Context there} for more
 * information.
 *
 * @see Context
 */
public class ApfloatContext extends Context<Apfloat, ApfloatContext> {

  public static final long DEFAULT_PRECISION_INCREMENT = 10;
  private final MathContext mathContext;

  /**
   * Constructor.
   *
   * @param mathContext The {@link MathContext} to use. This also contains the precision for teh
   *                    {@code Apfloats} when converting them from or to internal representation
   * @param precision   The precision used for internal calculations. Should be larger or equal than
   *                    the precision from the {@code MathContext}
   */
  public ApfloatContext(MathContext mathContext, long precision) {
    super(precision);
    this.mathContext = mathContext;
  }

  /**
   * Creates a minimal instance.
   * <p>
   * A minimal instance contains no variable, constant or function definition.
   *
   * @param mathContext The {@link MathContext} to use. This also contains the precision for teh
   *                    {@code Apfloats} when converting them from or to internal representation
   * @param precision   The precision used for internal calculations. Should be larger or equal than
   *                    the precision from the {@code MathContext}
   * @return The new instance
   */
  public static ApfloatContext minimal(MathContext mathContext, long precision) {
    return new ApfloatContext(mathContext, precision);
  }

  /**
   * Creates a minimal instance.
   * <p>
   * A minimal instance contains no variable, constant or function definition. The
   * {@link #precision()} is set to a value that is {@link #DEFAULT_PRECISION_INCREMENT} higher than
   * the precision form the {@code mathContext}
   *
   * @param mathContext The {@link MathContext} to use. This also contains the precision for teh
   *                    {@code Apfloats} when converting them from or to internal representation
   * @return The new instance
   */
  public static ApfloatContext minimal(MathContext mathContext) {
    return new ApfloatContext(mathContext,
        mathContext.getPrecision() + DEFAULT_PRECISION_INCREMENT);
  }

  /**
   * Creates a minimal instance using a {@link MathContext#DECIMAL128 DECIMAL128} context.
   * <p>
   * A minimal instance contains no variable, constant or function definition. The
   * {@link #precision()} is set to a value that is {@link #DEFAULT_PRECISION_INCREMENT} higher than
   * the precision form the {@code mathContext}
   *
   * @return The new instance
   */
  public static ApfloatContext minimal() {
    return minimal(MathContext.DECIMAL128);
  }

  /**
   * Creates a standard instance.
   * <p>
   * A standard instance provides the constants {@code pi} ({@code 3.14...}) and {@code e}
   * ({@code 2.71...} as well as the following commonly used functions: {@code sin}, {@code cos},
   * {@code tan}, {@code asin}, {@code acos}, {@code atan}, {@code sinh}, {@code cosh},
   * {@code tanh}, {@code asinh}, {@code acosh}, {@code atanh}, {@code log}, {@code ln}
   *
   * @param mathContext The {@link MathContext} to use. This also contains the precision for teh
   *                    {@code Apfloats} when converting them from or to internal representation
   * @param precision   The precision used for internal calculations. Should be larger or equal than
   *                    the precision from the {@code MathContext}
   * @return The new instance
   */
  public static ApfloatContext standard(MathContext mathContext, long precision) {
    return minimal(mathContext, precision)
        .constants(StandardConstants.PI(), StandardConstants.E())
        .withHyperbolicFunctions()
        .withInverseHyperbolicFunctions()
        .withTrigonometricFunctions()
        .withInverseTrigonometricFunctions()
        .withLogarithmFunctions();
  }

  /**
   * Creates a standard instance using the given {@link MathContext}.
   * <p>
   * A standard instance provides the constants {@code pi} ({@code 3.14...}) and {@code e}
   * ({@code 2.71...} as well as the following commonly used functions: {@code sin}, {@code cos},
   * {@code tan}, {@code asin}, {@code acos}, {@code atan}, {@code sinh}, {@code cosh},
   * {@code tanh}, {@code asinh}, {@code acosh}, {@code atanh}, {@code log}, {@code ln}.
   * <p>
   * The {@link #precision()} is set to a value that is {@link #DEFAULT_PRECISION_INCREMENT} higher
   * than the precision form the {@code mathContext}
   *
   * @param mathContext The {@link MathContext} to use. This also contains the precision for teh
   *                    {@code Apfloats} when converting them from or to internal representation
   * @return The new instance
   */
  public static ApfloatContext standard(MathContext mathContext) {
    return standard(mathContext, mathContext.getPrecision() + DEFAULT_PRECISION_INCREMENT);
  }

  /**
   * Creates a standard instance using a {@link MathContext#DECIMAL128 DECIMAL128} context.
   * <p>
   * A standard instance provides the constants {@code pi} ({@code 3.14...}) and {@code e}
   * ({@code 2.71...} as well as the following commonly used functions: {@code sin}, {@code cos},
   * {@code tan}, {@code asin}, {@code acos}, {@code atan}, {@code sinh}, {@code cosh},
   * {@code tanh}, {@code asinh}, {@code acosh}, {@code atanh}, {@code log}, {@code ln}.
   * <p>
   * The {@link #precision()} is set to a value that is {@link #DEFAULT_PRECISION_INCREMENT} higher
   * than the precision form the {@code mathContext}
   *
   * @return The new instance
   */
  public static ApfloatContext standard() {
    return standard(MathContext.DECIMAL128);
  }

  /**
   * Returns the associated {@code MathContext}.
   *
   * @return {@code MathContext}
   */
  public MathContext mathContext() {
    return mathContext;
  }

  @Override
  protected Apcomplex toApcomplex(Apfloat value) {
    return precisionHelper().valueOf(new Apcomplex(new Apfloat(value.toString(), precision())));
  }

  @Override
  protected Apfloat fromApcomplex(Apcomplex value) {
    if (!value.imag().equals(Apfloat.ZERO)) {
      throw new NotANumberException(value + " is not a real number");
    }
    return ApfloatMath.round(value.real(), mathContext.getPrecision(), mathContext.getRoundingMode());
  }

  @Override
  protected ApfloatContext newInstance() {
    return new ApfloatContext(mathContext, precision());
  }
}
