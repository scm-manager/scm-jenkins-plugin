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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class JenkinsEventRelay {

  private static final Logger LOG = LoggerFactory.getLogger(JenkinsEventRelay.class);
  public static final String EVENT_ENDPOINT = "scm-manager-hook/notify";

  private final ScmConfiguration configuration;

  private final JenkinsContext jenkinsContext;
  private final AdvancedHttpClient httpClient;
  private final Set<AdditionalServerIdentification> serverIdentifications;

  private final ObjectMapper mapper = new ObjectMapper();

  @Inject
  public JenkinsEventRelay(ScmConfiguration configuration, JenkinsContext jenkinsContext, AdvancedHttpClient httpClient, Set<AdditionalServerIdentification> serverIdentifications) {
    this.configuration = configuration;
    this.jenkinsContext = jenkinsContext;
    this.httpClient = httpClient;
    this.serverIdentifications = serverIdentifications;
  }

  void send(JenkinsEventDto eventDto) {
    doIfEnabled(() -> jenkinsContext.getServerUrl().ifPresent(s -> send(s, eventDto, jenkinsContext.getConfiguration().getUrl(), jenkinsContext.getConfiguration().getUsername(), jenkinsContext.getConfiguration().getApiToken())));
  }

  void send(Repository repository, JenkinsRepositoryEventDto eventDto) {
    doIfEnabled(() -> jenkinsContext.getServerUrl(repository).ifPresent(s -> send(s, eventDto, repository)));
  }

  private void doIfEnabled(Runnable callback) {
    if (!jenkinsContext.getConfiguration().isDisableEventTrigger()) {
      callback.run();
    }
  }

  private void send(String serverUrl, JenkinsRepositoryEventDto eventDto, Repository repository) {
    eventDto.setNamespace(repository.getNamespace());
    eventDto.setName(repository.getName());
    eventDto.setType(repository.getType());

    JenkinsConfiguration jenkinsConfiguration = jenkinsContext.getConfiguration(repository);
    send(serverUrl, eventDto, jenkinsConfiguration.getUrl(), jenkinsConfiguration.getUsername(), jenkinsConfiguration.getApiToken());
  }

  private void send(String serverUrl, JenkinsEventDto eventDto, String url, String username, String apiToken) {
    eventDto.setServer(configuration.getBaseUrl());

    List<AdditionalServerIdentification.Identification> identifications = serverIdentifications.stream().map(AdditionalServerIdentification::get).collect(Collectors.toList());
    eventDto.setIdentifications(identifications);

    try {
      String json = mapper.writer().writeValueAsString(eventDto);

      AdvancedHttpRequestWithBody request = httpClient.post(createEventHookUrl(serverUrl))
        .spanKind("Jenkins")
        .formContent().field("json", json).build();

      HeaderAppenders.appendCsrfCrumbHeader(httpClient, request, url, username, apiToken);

      request
        .request();
    } catch (IOException e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Failed to relay event to Jenkins server");
      }
    }
  }

  private String createEventHookUrl(String url) {
    if (!url.endsWith("/")) {
      url += "/";
    }
    return url + EVENT_ENDPOINT;
  }
}
