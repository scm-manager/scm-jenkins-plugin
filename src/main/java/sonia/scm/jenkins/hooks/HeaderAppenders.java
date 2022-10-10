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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.jenkins.CsrfCrumb;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.BaseHttpRequest;

import static sonia.scm.jenkins.hooks.CsrfCrumbRequester.getJenkinsCsrfCrumb;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderAppenders {

  static void appendAuthenticationHeader(BaseHttpRequest request, String username, String apiToken) {
    if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(apiToken)) {
      log.debug("added authentication for user {}", username);
      request.basicAuth(username, apiToken);
    } else {
      log.debug("skip authentication. username or api token is empty");
    }
  }

  static void appendCsrfCrumbHeader(AdvancedHttpClient client, BaseHttpRequest request, String url, String username, String apiToken) {
    if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(apiToken)) {
     log.debug("skip csrf crumb authentication. username or api token is empty");
      return;
    }
      CsrfCrumb crumb = getJenkinsCsrfCrumb(client, url, username, apiToken);
    if (crumb != null) {
      log.debug("add csrf crumb to api request");
      request.header(crumb.getCrumbRequestField(), crumb.getCrumb());
    }
  }
}
