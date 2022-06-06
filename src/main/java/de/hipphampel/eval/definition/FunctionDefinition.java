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

import java.util.List;
import de.hipphampel.eval.Context;
import org.apfloat.Apcomplex;


/**
 * Interface for a function definition.
 * <p>
 * In terms of this library, a function accepts one or more {@link Apcomplex} numbers as arguments
 * and returns an {@code Apcomplex} number as result. Function must have no side effects and are
 * idempotent: calling a function twice with the same arguments must always yield the same result
 */
public interface FunctionDefinition extends Definition {

  /**
   * The minimum number of arguments this function accepts.
   *
   * @return Minimum number of arguments, at least 1
   */
  int minArgs();

  /**
   * The maximum number of arguments this function accepts
   *
   * @return Maximum number of arguments, at least {@link #minArgs()}
   */
  int maxArgs();

  /**
   * Evaluates this function.
   *
   * @param context The {@link Context} to use.
   * @param args    The arguments, the number of arguments must respect the {@link #minArgs()} and
   *                {@link #maxArgs()} settings
   * @return The function result
   */
  Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args);
}
