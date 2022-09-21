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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import java.io.IOException;

public class GlobalHookUtils {

  private GlobalHookUtils() {}

  private static final Logger LOG = LoggerFactory.getLogger(GlobalHookUtils.class);

  static void sendRequest(GlobalJenkinsConfiguration config, AdvancedHttpClient client, String url) {
    try {
      LOG.info("try to access url {}", url);
      AdvancedHttpRequest request = client.get(url)
        .spanKind("Jenkins");
      HeaderAppenders.appendAuthenticationHeader(request, config.getUsername(), config.getApiToken());
      HeaderAppenders.appendCsrfCrumbHeader(client, request, config.getUrl(), config.getUsername(), config.getApiToken());
      AdvancedHttpResponse response = request.request();
      int statusCode = response.getStatus();
      LOG.info("request returned {}", statusCode);
    } catch (IOException ex) {
      LOG.error("could not execute http request", ex);
    }
  }
}
