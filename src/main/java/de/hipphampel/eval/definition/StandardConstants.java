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

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

/**
 * Definition of certain standard constants.
 */
public class StandardConstants {

  /**
   * PI.
   */
  public static Constant PI() {
    return new Constant("pi",
        context -> new Apcomplex(ApfloatMath.pi(context.precision())));
  }

  /**
   * The imaginary unit {@code i}.
   */
  public static Constant I() {
    return new Constant("i",
        context -> Apcomplex.I.precision(context.precision()));
  }

  /**
   * The Euler number {@code e}.
   */
  public static Constant E() {
    return new Constant("e",
        context -> new Apcomplex(ApfloatMath.exp(new Apfloat(1, context.precision()))));
  }

  private StandardConstants() {
  }


}
