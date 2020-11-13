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
package sonia.scm.jenkins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;

import static sonia.scm.jenkins.HeaderAppenders.appendAuthenticationHeader;
import static sonia.scm.jenkins.Urls.escape;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsrfCrumbRequester {

  static CsrfCrumb getJenkinsCsrfCrumb(AdvancedHttpClient client, String baseUrl, String username, String apiToken) {
    String url = createCrumbUrl(baseUrl);
    log.debug("fetch csrf crumb from {}", url);

    AdvancedHttpRequest request = client.get(url).spanKind("Jenkins");
    appendAuthenticationHeader(request, username, apiToken);

    CsrfCrumb crumb = null;
    try {
      AdvancedHttpResponse response = request.request();
      int sc = response.getStatus();
      if (sc != 200) {
        log.warn("jenkins crumb endpoint returned status code {}", sc);
      } else {
        crumb = parseCsrfCrumbResponse(response);
      }
    } catch (IOException ex) {
      log.warn("failed to fetch csrf crumb", ex);
    }

    return crumb;
  }

  private static CsrfCrumb parseCsrfCrumbResponse(AdvancedHttpResponse response) throws IOException {
    CsrfCrumb csrfCrumb;
    InputStream content = null;
    try {
      content = response.contentAsStream();
      csrfCrumb = CsrfCrumbParser.parse(content);
    } finally {
      IOUtil.close(content);
    }
    return csrfCrumb;
  }

  private static String createCrumbUrl(String url) {
    return createBaseUrl(url).concat("crumbIssuer/api/xml");
  }

  private static String createBaseUrl(String url) {
    if (!url.endsWith("/")) {
      return url.concat("/");
    }
    return url;
  }
}
