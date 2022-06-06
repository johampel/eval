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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.eval.definition.StandardConstants;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DoubleContextTest {


  @Test
  public void precision() {
    DoubleContext context = DoubleContext.minimal()
        .constants(StandardConstants.PI(), StandardConstants.E());

    assertThat(context.evaluate("pi")).isCloseTo(Math.PI, Offset.offset(1e-15d));
    assertThat(context.evaluate("e")).isCloseTo(Math.E, Offset.offset(1e-15d));
  }

  @Test
  public void minimal() {
    DoubleContext context = DoubleContext.minimal();
    assertThat(context.functionNames()).isEmpty();
    assertThat(context.valueNames()).isEmpty();
  }

  @Test
  public void standard() {
    DoubleContext context = DoubleContext.standard();
    assertThat(context.functionNames()).containsExactlyInAnyOrder("tan",
        "ln",
        "sinh",
        "atanh",
        "log",
        "cos",
        "acosh",
        "acos",
        "atan",
        "cosh",
        "tanh",
        "sin",
        "asin",
        "asinh");
    assertThat(context.valueNames()).containsExactlyInAnyOrder("e", "pi");
  }

  @ParameterizedTest
  @CsvSource({
      "'(2+3)*4',         20.0",
      "'sin(pi/4)*2^0.5',  1.0",
  })
  public void evaluate(String expression, double result) {
    DoubleContext context = DoubleContext.standard();
    assertThat(context.evaluate(expression)).isEqualTo(result);
  }

  @Test
  public void complexResult() {
    DoubleContext context = DoubleContext.standard();
    assertThat(context.evaluate("-1^0.5")).isNaN();
  }

  @Test
  public void newInstance() {
    DoubleContext context = DoubleContext.standard();
    assertThat(context.newInstance()).isNotNull();
    assertThat(context.newInstance()).isNotSameAs(context);
  }
}
