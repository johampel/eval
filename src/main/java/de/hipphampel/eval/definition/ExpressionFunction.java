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

import de.hipphampel.eval.ApcomplexContext;
import de.hipphampel.eval.Context;
import de.hipphampel.eval.exception.EvalException;
import de.hipphampel.eval.expr.Expression;
import java.util.List;
import org.apfloat.Apcomplex;

/**
 * A {@link FunctionDefinition} based on an {@link Expression}.
 * <p>
 * A function of this type is based on an {@code Expression}. Instances of this class are always
 * bound to a {@link Context}, since it is allowed to refer to constants and functions defined in
 * the bound {@code Context}. So an instance can be used in conjunction with the {@code Context}
 * passed to the constructor or a copy of it (since a copy always contains at least the same
 * constants and functions),
 * <p>
 * When evaluating the function, an internal {@code Context} is used, which is a copy of the
 * original one, that contains all definitions except the variable definitions. The function
 * parameters are realized as variables declared for this internal context only.
 */
public class ExpressionFunction implements FunctionDefinition {

  private final String name;
  private final ApcomplexContext innerContext;
  private final List<String> parameters;
  private final Expression definition;

  /**
   * Constructor.
   *
   * @param name       The name of the function.
   * @param context    The {@code Context}
   * @param parameters The paramter names, these can be used in the function definition as they were
   *                   variables.
   * @param definition The string literal with the function definition. THis is parsed using
   *                   {@code Context}
   */
  public ExpressionFunction(String name, Context<?, ?> context, List<String> parameters,
      String definition) {
    this.name = name;
    this.innerContext = context.copyAsApcomplexContext(false).variables(parameters);
    this.parameters = parameters;
    this.definition = innerContext.parse(definition).simplify(innerContext);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public int minArgs() {
    return parameters.size();
  }

  @Override
  public int maxArgs() {
    return parameters.size();
  }

  @Override
  public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
    if (args.size() != parameters.size()) {
      throw new EvalException(String.format("Invalid argument count, expected %d, but got %d",
          parameters.size(), args.size()));
    }
    for (int i = 0; i < args.size(); i++) {
      innerContext.variable(parameters.get(i), args.get(i));
    }
    return definition.evaluate(this.innerContext);
  }

}
