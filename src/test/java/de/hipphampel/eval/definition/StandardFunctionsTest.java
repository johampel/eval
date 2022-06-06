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

import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.eval.DoubleContext;
import de.hipphampel.eval.parser.ParseMode;
import org.apfloat.Apfloat;
import org.apfloat.FixedPrecisionApfloatHelper;
import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.RoundingMode;

public class StandardFunctionsTest {

  @ParameterizedTest
  @CsvSource({
      "sin(0),     0.0",
      "sin(pi/2),  1.0",
      "sin(pi),    0.0",
      "sin(3pi/2), -1.0",

  })
  public void sin(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.SIN);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "cos(0),     1.0",
      "cos(pi/2),  0.0",
      "cos(pi),    -1.0",
      "cos(3pi/2), 0.0",

  })
  public void cos(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.COS);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "tan(0),     0.0",
      "tan(pi/4),  1.0",
      "tan(pi/-4), -1.0",
  })
  public void tan(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.TAN);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "asin(0),     0.0",
      "asin(1),     1.5707963267948963",
      "asin(-1),    -1.5707963267948963",

  })
  public void asin(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ASIN);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "acos(0),     1.5707963267948963",
      "acos(1),     0.0",
      "acos(-1),    3.1415926535897927",
  })
  public void acos(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ACOS);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "atan(0),     0.0",
      "atan(1),     0.7853981633974483",
      "atan(-1),    -0.7853981633974483",
  })
  public void atan(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ATAN);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }


  @ParameterizedTest
  @CsvSource({
      "sinh(0),   0.0",
      "sinh(-1),  -1.1752011936438014",
      "sinh(1),   1.1752011936438014",
  })
  public void sinh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.SINH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "cosh(0),   1.0",
      "cosh(-1),  1.5430806348152435",
      "cosh(1),   1.5430806348152435",
  })
  public void cosh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.COSH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "tanh(0),     0.0",
      "tanh(1),     0.7615941559557649",
      "tanh(-1),    -0.7615941559557649",
  })
  public void tanh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.TANH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "asinh(0),   0.0",
      "asinh(-1),  -0.881373587019543",
      "asinh(1),   0.881373587019543",
  })
  public void asinh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ASINH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "acosh(2),   1.3169578969248168",
      "acosh(1),   0.0",
  })
  public void acosh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ACOSH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "atanh(0),     0.0",
      "atanh(0.5),   0.5493061443340549",
      "atanh(-0.5),  -0.5493061443340549",
  })
  public void atanh(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.PI())
        .function(StandardFunctions.ATANH);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "ln(1),   0.0",
      "ln(e),   1",
      "ln(1/e), -1",
  })
  public void ln(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.E())
        .function(StandardFunctions.LN);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'log(1, 10)',   0.0",
      "'log(256, 2)',  8",
  })
  public void log(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .function(StandardFunctions.LOG);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'real(2i)',      0.0",
      "'real(3+4i)',    3.0",
      "'real(5)',       5.0",
  })
  public void real(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.I())
        .function(StandardFunctions.REAL);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'imag(2i)',      2.0",
      "'imag(3+4i)',    4.0",
      "'imag(5)',       0.0",
  })
  public void imag(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.I())
        .function(StandardFunctions.IMAG);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'norm(2i)',      4.0",
      "'norm(3+4i)',    25.0",
      "'norm(4)',       16.0",
  })
  public void norm(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.I())
        .function(StandardFunctions.NORM);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'arg(2i)',      1.5707963267948963",
      "'arg(2+2i)',    0.7853981633974483",
      "'arg(4)',       0.0",
  })
  public void arg(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.I())
        .function(StandardFunctions.ARG);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }

  @ParameterizedTest
  @CsvSource({
      "'abs(2i)',      2.0",
      "'abs(-2-2i)',   2.82842712474619",
      "'abs(-4)',      4.0",
  })
  public void abs(String expression, double expected) {
    DoubleContext context =DoubleContext.minimal()
        .parseMode(ParseMode.SIMPLIFIED)
        .constants(StandardConstants.I())
        .function(StandardFunctions.ABS);
    assertThat(context.evaluate(expression)).isCloseTo(expected, Offset.offset(1e-15d));
  }
}
