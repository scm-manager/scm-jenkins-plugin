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
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.util.Util;

import static sonia.scm.jenkins.CsrfCrumbRequester.getJenkinsCsrfCrumb;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HeaderAppenders {

  static void appendAuthenticationHeader(BaseHttpRequest request, String username, String apiToken) {
    // check for authentication parameters

    if (Util.isNotEmpty(username) && Util.isNotEmpty(apiToken)) {
      log.debug("added authentication for user {}", username);

      // add basic authentication header
      request.basicAuth(username, apiToken);
    } else {
      log.debug("skip authentication. username or api token is empty");
    }
  }

  static void appendCsrfCrumbHeader(AdvancedHttpClient client, BaseHttpRequest request, String url, String username, String apiToken) {
    CsrfCrumb crumb = getJenkinsCsrfCrumb(client, url, username, apiToken);
    if (crumb != null) {
      log.debug("add csrf crumb to api request");
      request.header(crumb.getCrumbRequestField(), crumb.getCrumb());
    }
  }
}
