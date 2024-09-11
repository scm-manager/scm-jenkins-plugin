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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
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
    doIfEnabled(() -> jenkinsContext.getServerUrl().ifPresent(s -> send(s, eventDto)));
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

    send(serverUrl, eventDto);
  }

  private void send(String serverUrl, JenkinsEventDto eventDto) {
    eventDto.setServer(configuration.getBaseUrl());

    List<AdditionalServerIdentification.Identification> identifications = serverIdentifications.stream().map(AdditionalServerIdentification::get).collect(Collectors.toList());
    eventDto.setIdentifications(identifications);

    try {
      String json = mapper.writer().writeValueAsString(eventDto);

      httpClient.post(createEventHookUrl(serverUrl))
        .spanKind("Jenkins")
        .formContent().field("json", json).build()
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
