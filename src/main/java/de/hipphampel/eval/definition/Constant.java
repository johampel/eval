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

import de.hipphampel.eval.Context;
import java.util.Objects;
import java.util.function.Function;
import org.apfloat.Apcomplex;

/**
 * Defines a constant.
 * <p>
 * A {@code Constant} is a {@link ValueDefinition} with an immutable value
 */
public class Constant implements ValueDefinition {

  private final String name;
  private final Function<Context<?, ?>, Apcomplex> provider;
  private Apcomplex value;

  /**
   * Constructor.
   *
   * @param name  The name of the constant
   * @param value The value of the constant
   */
  public Constant(String name, Apcomplex value) {
    this(name, context -> context.precisionHelper().valueOf(Objects.requireNonNull(value)));
  }

  /**
   * Constructor,
   *
   * @param name     The name of the constant
   * @param provider A provider that computes the constant value
   */
  public Constant(String name, Function<Context<?, ?>, Apcomplex> provider) {
    this.name = Objects.requireNonNull(name);
    this.provider = Objects.requireNonNull(provider);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Apcomplex value(Context<?, ?> context) {
    if (value == null) {
      value = provider.apply(context);
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Constant constant = (Constant) o;
    return Objects.equals(name, constant.name) && Objects.equals(value, constant.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "Constant{" + "name='" + name + '\'' + ", provider=" + provider + ", value=" + value
        + '}';
  }
}
