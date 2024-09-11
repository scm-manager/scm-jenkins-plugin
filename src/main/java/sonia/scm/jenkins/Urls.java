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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Urls {

  /**
   * Escapes a complete url. The method is visible to the package for testing.
   *
   * @param urlString url
   * @return escaped url
   */
  static String escape(String urlString) {
    String escapedUrl = urlString;

    try {
      URL url = new URL(urlString);
      //J-
      URI uri = new URI(
        url.getProtocol(),
        url.getUserInfo(),
        url.getHost(),
        url.getPort(),
        url.getPath(),
        url.getQuery(),
        url.getRef()
      );
      //J+

      escapedUrl = uri.toString();
    } catch (URISyntaxException | MalformedURLException ex) {
      log.warn("could not escaped url", ex);
    }

    return escapedUrl;
  }

  /**
   * Fix duplicated slashes in repository url.
   *
   * @param url url to fix
   * @return fixed url
   * @see "<a target="_blank" href="https://bitbucket.org/sdorra/scm-manager/issue/510/wrong-git-notification-url-to-many-slashes">Issue 510</a>"
   */
  static String fix(String url) {
    int index = url.indexOf("://");

    // absolute url
    if (index > 0) {
      String suffix = url.substring(index + 3).replaceAll("//", "/");

      url = url.substring(0, index + 3).concat(suffix);
    }

    // relative url
    else {
      url = url.replaceAll("//", "/");
    }

    return url;
  }
}
