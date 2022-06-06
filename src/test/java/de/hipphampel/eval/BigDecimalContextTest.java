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
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BigDecimalContextTest {

  @ParameterizedTest
  @CsvSource({
      "20, 20,  pi, 3.1415926535897932384",
      "20, 30,  pi, 3.1415926535897932385",
      "40, 40,  pi, 3.141592653589793238462643383279502884197",
      "40, 50,  pi, 3.141592653589793238462643383279502884197",
      "20, 20,  e,  2.7182818284590452353",
      "20, 30,  e,  2.7182818284590452354",
      "40, 40,  e,  2.718281828459045235360287471352662497757",
      "40, 50,  e,  2.718281828459045235360287471352662497757",
  })
  public void precision(int extPrecision, int precision, String constant, BigDecimal value) {
    BigDecimalContext context = BigDecimalContext.minimal(new MathContext(extPrecision), precision)
        .constants(StandardConstants.PI(), StandardConstants.E());
    assertThat(context.evaluate(constant)).isEqualTo(value);
  }

  @Test
  public void minimal() {
    BigDecimalContext context = BigDecimalContext.minimal();
    assertThat(context.functionNames()).isEmpty();
    assertThat(context.valueNames()).isEmpty();
  }

  @Test
  public void standard() {
    BigDecimalContext context = BigDecimalContext.standard();
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
      "'(2+3)*4',          2e+1",
      "'sin(pi/4)*2^0.5',  1",
  })
  public void evaluate(String expression, BigDecimal result) {
    BigDecimalContext context = BigDecimalContext.standard();
    assertThat(context.evaluate(expression)).isEqualTo(result);
  }

  @Test
  public void complexResult() {
    BigDecimalContext context = BigDecimalContext.standard();
    assertThatThrownBy(() -> context.evaluate("-1^0.5")).isInstanceOf(NotANumberException.class);
  }


  @Test
  public void newInstance() {
    BigDecimalContext context = BigDecimalContext.standard();
    assertThat(context.newInstance()).isNotNull();
    assertThat(context.newInstance()).isNotSameAs(context);
  }

}
