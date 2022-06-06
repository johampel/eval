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

import java.util.List;
import java.util.stream.Collectors;
import de.hipphampel.eval.definition.FunctionDefinition;
import de.hipphampel.eval.parser.ParseMode;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.FixedPrecisionApcomplexHelper;
import org.junit.jupiter.api.Test;

public class Examples {

  @Test
  public void example1() {
    DoubleContext context = DoubleContext.standard();
    double result = context.evaluate("(2+3)*5");
    System.out.println(result);
    assertThat(result).isEqualTo(25);
  }

  @Test
  public void example2() {
    DoubleContext context = DoubleContext.standard();
    double result = context.evaluate("sin(pi/2)");
    System.out.println(result);
    assertThat(result).isEqualTo(1);
  }

  @Test
  public void example3() {
    DoubleContext context = DoubleContext.standard();
    context.constant("two", 2.0)
        .function("f", List.of("x", "y"), "x^y + y^x");
    double result = context.evaluate("f(two, 3)");
    System.err.println(result);
    assertThat(result).isEqualTo(17);
  }

  @Test
  public void example4() {
    DoubleContext context = DoubleContext.standard();
    context.constant("two", 2.0)
        .function("f", List.of("x", "y"), "x^y + y^x");

    context.variable("x", 1.0);
    double result = context.evaluate("f(x, two)");
    System.err.println(result);
    assertThat(result).isEqualTo(3);

    context.variable("x", 2.0);
    result = context.evaluate("f(x, two)");
    System.err.println(result);
    assertThat(result).isEqualTo(8);
  }

  @Test
  public void example5() {
    ApcomplexContext context = ApcomplexContext.standard();
    Apcomplex result = context.evaluate("(-1)^0.5");
    System.err.println(result);
    assertThat(result).isEqualTo(Apcomplex.I);
  }

  @Test
  public void example6() {
    ApcomplexContext context = ApcomplexContext.standard();
    Apcomplex result = context.evaluate("-2^2");
    System.err.println(result);
    context.parseMode(ParseMode.SIMPLIFIED);
    result = context.evaluate("-2^2");
    System.err.println(result);
  }


  public class Geomean implements FunctionDefinition {

    @Override
    public String name() {
      return "geomean";
    }

    @Override
    public int minArgs() {
      return 1;
    }

    @Override
    public int maxArgs() {
      return Integer.MAX_VALUE;
    }

    @Override
    public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
      FixedPrecisionApcomplexHelper helper = context.precisionHelper();
      Apcomplex product = args.stream().reduce(Apcomplex.ONE, helper::multiply);
      Apcomplex exp = helper.divide(Apcomplex.ONE, new Apcomplex(new Apfloat(args.size())));
      return context.precisionHelper().pow(product, exp);
    }
  }
}
