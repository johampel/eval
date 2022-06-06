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
import org.apfloat.Apcomplex;

/**
 * Represents a {@link ValueDefinition} with a mutable value.
 * <p>
 * In opposite to a {@link Constant} this instance allows to modify the value by setting it via
 * {@link #value(Context, Apcomplex)}
 */
public class Variable implements ValueDefinition {

  private final String name;
  private Apcomplex value;

  /**
   * Constructor.
   * <p>
   * Creates an instance with the initial value {@code 0},
   *
   * @param name The name of the variabel
   */
  public Variable(String name) {
    this.name = Objects.requireNonNull(name);
    this.value = Apcomplex.ZERO;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Apcomplex value(Context<?, ?> context) {
    return value;
  }

  /**
   * Sets the value of this instance.
   *
   * @param context The {@link Context}. This is used to apply the correct precision to the value
   * @param value   The new value
   */
  public void value(Context<?, ?> context, Apcomplex value) {
    this.value = Objects.requireNonNull(value).precision(context.precision());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Variable variable = (Variable) o;
    return Objects.equals(name, variable.name) && Objects.equals(value, variable.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "Variable{" + "name='" + name + '\'' + ", value=" + value + '}';
  }
}
