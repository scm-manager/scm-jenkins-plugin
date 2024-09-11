/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.jenkins;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.jenkins.Urls.escape;
import static sonia.scm.jenkins.Urls.fix;

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
