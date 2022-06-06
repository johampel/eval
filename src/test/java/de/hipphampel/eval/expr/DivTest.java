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

import static de.hipphampel.eval.expr.ExpressionFactory.val;
import static org.assertj.core.api.Assertions.assertThat;

import de.hipphampel.eval.ApcomplexContext;
import de.hipphampel.eval.parser.ParserFactory;
import org.apfloat.Apcomplex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DivTest {

  private static final ApcomplexContext context = new ApcomplexContext(10);

  static {
    context.constant("c", Apcomplex.ONE)
        .variable("v");
  }

  @Test
  public void evaluate() {
    Div expr = new Div(val("1"), val("2"));
    assertThat(expr.evaluate(context)).isEqualTo(new Apcomplex("0.5"));
  }

  @ParameterizedTest
  @CsvSource({
      // before,     after
      "1/2,          'Value[value=5e-1]'",
      "(c+1)/(2+v),  'Div[left=Value[value=2], right=Add[left=Value[value=2], right=ValueName[name=v]]]'",
      "(v+1)/(2+c),  'Div[left=Add[left=ValueName[name=v], right=Value[value=1]], right=Value[value=3]]'"
  })
  public void simplify(String before, String after) {
    Div expr = ParserFactory.expressionParserStandard(context).end().parse(before).get();
    Expression simple = expr.simplify(context);
    assertThat(simple.toString()).isEqualTo(after);
  }
}
