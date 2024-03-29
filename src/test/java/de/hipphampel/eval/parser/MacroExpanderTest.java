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
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class MacroExpanderTest {

  @Test
  public void ENVIRONMENT_VARIABLES() {
    try(MockedStatic<Utils> utils = mockStatic(Utils.class)) {
      utils.when(() -> Utils.getenv("ABC")).thenReturn("123");
      utils.when(() -> Utils.getenv("DEF")).thenReturn("${ABC}*456");
      Context<?,?> context = mock(Context.class);

      assertThat(MacroExpander.ENVIRONMENT_VARIABLES.expand(context, "${ABC}/${DEF}"))
          .isEqualTo("123/123*456");
    }
  }
}
