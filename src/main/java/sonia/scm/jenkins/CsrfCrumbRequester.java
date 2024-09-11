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
