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
 * Represents a multiplication.
 *
 * @param left  Left operand
 * @param right Right operand
 */
public record Mul(Expression left, Expression right) implements BinaryExpression {

  @Override
  public Apcomplex evaluate(Context<?,?> context, Apcomplex left, Apcomplex right) {
    return left.multiply(right);
  }

  @Override
  public Expression simplify(Context<?, ?> context) {
    if (isInvariant(context)) {
      return new Value(evaluate(context));
    }else {
      return new Mul(left().simplify(context), right().simplify(context));
    }
  }
}
