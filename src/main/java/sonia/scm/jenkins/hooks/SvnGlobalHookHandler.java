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

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.jenkins.JenkinsHookHandler;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.Optional;

import static sonia.scm.jenkins.hooks.HeaderAppenders.appendCsrfCrumbHeader;

public class SvnGlobalHookHandler implements JenkinsHookHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SvnGlobalHookHandler.class);
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory repositoryServiceFactory;
  private final GlobalJenkinsConfiguration configuration;
  private final SvnHookHandlerHelper helper;

  public SvnGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider, RepositoryServiceFactory repositoryServiceFactory, GlobalJenkinsConfiguration configuration) {
    this.httpClientProvider = httpClientProvider;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.configuration = configuration;
    this.helper = new SvnHookHandlerHelper(repositoryServiceFactory);
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (Util.isNotEmpty(configuration.getUrl()) && !configuration.isDisableSubversionTrigger()) {
      try (RepositoryService repositoryService = repositoryServiceFactory.create(event.getRepository())) {
        Optional<String> uuid = helper.lookupUUID(repositoryService);
        if (uuid.isPresent()) {
          String url = helper.createUrl(event, configuration.getUrl(), uuid.get());
          String content = helper.getContent(event);
          sendRequest(url, content);
        } else {
          LOG.error("Could not send request: No uuid for svn repository found");
        }
      }
    }
  }

  private void sendRequest(String url, String content) {
    try {
      AdvancedHttpRequestWithBody request = httpClientProvider.get()
        .post(url)
        .spanKind("Jenkins")
        .header("Content-Type", "text/plain;charset=UTF-8")
        .stringContent(content);

      request.basicAuth(configuration.getUsername(), configuration.getApiToken());
      appendCsrfCrumbHeader(httpClientProvider.get(), request, configuration.getUrl(), configuration.getUsername(), configuration.getApiToken());

      AdvancedHttpResponse response = request.request();

      int statusCode = response.getStatus();

      if (LOG.isInfoEnabled()) {
        LOG.info("request returned {}", statusCode);
      }
    } catch (IOException e) {
      LOG.error("could not execute http request", e);
    }
  }
}
