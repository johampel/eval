package de.hipphampel.eval.parser;

/*-
 * #%L
 * eval
 * %%
 * Copyright (C) 2022 - 2023 Johannes Hampel
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
import de.hipphampel.eval.expr.Expression;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Expands a text to an {@link  Expression}.
 * <p>
 * When providing a {@code MacroExpander} to a {@link Context} via
 * {@link Context#macroExpander(MacroExpander)}, texts are preprocessed by the {@code MacroExpander}
 * before parsing it.
 * <p>
 * Such a preprocessing is a pure text replacement, before it comes to actual parsing the
 * expression, a concrete application of this feature is the {@link #ENVIRONMENT_VARIABLES}
 * {@code MacroExpander}, its documentation provides also some examples.
 */
@FunctionalInterface
public interface MacroExpander {

  /**
   * Expands {@code macro}.
   *
   * @param context The evaluation context.
   * @param macro   The macro literal
   * @return The expansion.
   */
  String expand(Context<?, ?> context, String macro);

  /**
   * A {@link MacroExpander} doing nothing.
   */
  MacroExpander NOP = (context, macro) -> macro;

  /**
   * A {@link MacroExpander} expanding shell variables.
   * <p>
   * This implementation allows to replace environment variables in a expression literal. Assume you
   * have the two variables {@code A=1+2} and {@code B=3} defined in your environment, then - if
   * using this {@code MacroExpander} - you may write:
   * <pre>
   *   ${A} * ${B}
   * </pre>
   * which expands to
   * <pre>
   *   1+2 * 3
   * </pre>
   * then.
   * <p>
   * Note that macro expansion works recursively, so having also the variable {@code C=${A}/${B}} in
   * place, the expression
   * <pre>
   *   ${C} + ${B}
   * </pre>
   * which expands to
   * <pre>
   *   1+2/3 + 3
   * </pre>
   */
  MacroExpander ENVIRONMENT_VARIABLES = (context, expression) -> {
    Pattern pattern = Pattern.compile("(.*?)\\$\\{([^}]+)}(.*)");
    Matcher matcher = pattern.matcher(expression);
    while (matcher.matches()) {
      String variableName = matcher.group(2);
      String variableValue = Utils.getenv(variableName);
      expression =
          matcher.group(1) + (variableValue == null ? "" : variableValue) + matcher.group(3);
      matcher = pattern.matcher(expression);
    }
    return expression;
  };
}
