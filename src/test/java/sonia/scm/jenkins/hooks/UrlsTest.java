/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.jenkins.hooks;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.jenkins.hooks.Urls.escape;
import static sonia.scm.jenkins.hooks.Urls.fix;

class UrlsTest {

  @Test
  void testFixUrl() {
    assertThat(fix("http://www.scm-manager.org/scm/hg")).isEqualTo("http://www.scm-manager.org/scm/hg");
    assertThat(fix("http://www.scm-manager.org/scm//hg")).isEqualTo("http://www.scm-manager.org/scm/hg");
    assertThat(fix("http://www.scm-manager.org//scm//hg")).isEqualTo("http://www.scm-manager.org/scm/hg");
    assertThat(fix("https://www.scm-manager.org//scm//hg")).isEqualTo("https://www.scm-manager.org/scm/hg");
    assertThat(fix("https://www.scm-manager.org/scm/hg")).isEqualTo("https://www.scm-manager.org/scm/hg");
    assertThat(fix("/scm/hg")).isEqualTo("/scm/hg");
    assertThat(fix("//scm/hg")).isEqualTo("/scm/hg");
    assertThat(fix("//scm//hg")).isEqualTo("/scm/hg");
  }

  @org.junit.jupiter.api.Test
  void testEscape() {
    assertThat(escape("https://ci.scm-manager.org")).isEqualTo("https://ci.scm-manager.org");
    assertThat(escape("https://ci.scm-manager.org/path")).isEqualTo("https://ci.scm-manager.org/path");
    assertThat(escape("https://ci.scm-manager.org/some/deep/path")).isEqualTo("https://ci.scm-manager.org/some/deep/path");
    assertThat(escape("https://ci.scm-manager.org/path?with=query&param=true")).isEqualTo("https://ci.scm-manager.org/path?with=query&param=true");
    assertThat(escape("https://ci.scm-manager.org/with spaces")).isEqualTo("https://ci.scm-manager.org/with%20spaces");
  }
}
