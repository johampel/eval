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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.apfloat.Apcomplex;
import org.junit.jupiter.api.Test;

public class ExpressionFactoryTest {

  private static final Expression a = new ValueName("a");
  private static final Expression b = new ValueName("b");
  private static final Expression c = new ValueName("c");
  private static final Expression d = new ValueName("d");

  @Test
  public void add() {
    Add result = ExpressionFactory.add(a, b);
    assertThat(result).isEqualTo(new Add(a, b));
  }

  @Test
  public void div() {
    Div result = ExpressionFactory.div(a, b);
    assertThat(result).isEqualTo(new Div(a, b));
  }

  @Test
  public void fn_elipsis() {
    FunctionCall result = ExpressionFactory.fn("function", a, b, c);
    assertThat(result).isEqualTo(new FunctionCall("function", List.of(a, b, c)));
  }

  @Test
  public void fn_list() {
    FunctionCall result = ExpressionFactory.fn("function", List.of(a, b, c));
    assertThat(result).isEqualTo(new FunctionCall("function", List.of(a, b, c)));
  }

  @Test
  public void mul() {
    Mul result = ExpressionFactory.mul(a, b);
    assertThat(result).isEqualTo(new Mul(a, b));
  }

  @Test
  public void neg() {
    Neg result = ExpressionFactory.neg(a);
    assertThat(result).isEqualTo(new Neg(a));
  }

  @Test
  public void pow() {
    Pow result = ExpressionFactory.pow(a, b);
    assertThat(result).isEqualTo(new Pow(a, b));
  }

  @Test
  public void sub() {
    Sub result = ExpressionFactory.sub(a, b);
    assertThat(result).isEqualTo(new Sub(a, b));
  }

  @Test
  public void val_value() {
    Value val = ExpressionFactory.val(Apcomplex.I);
    assertThat(val).isEqualTo(new Value(Apcomplex.I));
  }

  @Test
  public void val_str() {
    Value val = ExpressionFactory.val("1.2");
    assertThat(val).isEqualTo(new Value(new Apcomplex("1.2")));
  }

  @Test
  public void var() {
    ValueName var = ExpressionFactory.var("abc");
    assertThat(var).isEqualTo(new ValueName("abc"));
  }
}
