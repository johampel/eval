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

import de.hipphampel.eval.ApcomplexContext;
import de.hipphampel.eval.parser.ParserFactory;
import org.apfloat.Apcomplex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ValueNameTest {

  private static final ApcomplexContext context = new ApcomplexContext(10);

  static {
    context.constant("c", Apcomplex.ONE)
        .variable("v");
  }


  static {
    context.variable("i", Apcomplex.I);
  }

  @Test
  public void evaluate() {
    ValueName expr = new ValueName("i");
    assertThat(expr.evaluate(context)).isEqualTo(Apcomplex.I);
  }

  @ParameterizedTest
  @CsvSource({
      // before, after
      "c,        'Value[value=1]'",
      "v,        'ValueName[name=v]'",
  })
  public void simplify(String before, String after) {
    ValueName expr = ParserFactory.expressionParserStandard(context).end().parse(before).get();
    Expression simple = expr.simplify(context);
    assertThat(simple.toString()).isEqualTo(after);
  }

}
