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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.FormContentBuilder;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.jenkins.JenkinsEventRelay.EVENT_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class JenkinsEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  static final String SERVER_URL = "http://scm-manager.org";

  @Mock
  private ScmConfiguration configuration;
  @Mock
  private JenkinsContext jenkinsContext;
  @Mock
  private AdvancedHttpClient httpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock
  private FormContentBuilder formContentBuilder;

  private Set<AdditionalServerIdentification> serverIdentifications = new HashSet<>();

  @Captor
  private ArgumentCaptor<String> captor;

  private JenkinsEventRelay sender;

  @BeforeEach
  void initScmConfig() {
    lenient().when(configuration.getBaseUrl()).thenReturn(SERVER_URL);
  }

  @BeforeEach
  void createSender() {
    sender = new JenkinsEventRelay(configuration, jenkinsContext, httpClient, serverIdentifications);
  }

  @Test
  void shouldNotSendEventsWhenTriggerDisabled() {
    GlobalJenkinsConfiguration configuration = new GlobalJenkinsConfiguration();
    configuration.setDisableEventTrigger(true);
    when(jenkinsContext.getConfiguration()).thenReturn(configuration);

    sender.send(REPOSITORY, new JenkinsRepositoryEventDto(EventTarget.SOURCE, Collections.singletonList(new ProtocolResolverTest.DummyScmProtocol())));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldNotSendWithoutValidConfiguration() {
    when(jenkinsContext.getConfiguration()).thenReturn(new GlobalJenkinsConfiguration());
    when(jenkinsContext.getServerUrl(REPOSITORY)).thenReturn(Optional.empty());

    sender.send(REPOSITORY, new JenkinsRepositoryEventDto(EventTarget.SOURCE, Collections.singletonList(new ProtocolResolverTest.DummyScmProtocol())));

    verify(httpClient, never()).post(anyString());
  }

  @Nested
  class ForExpectedRequest {

    @BeforeEach
    void mockRequest() throws IOException {
      when(request.spanKind("Jenkins").request()).thenReturn(response);
      when(request.formContent()).thenReturn(formContentBuilder);
      when(formContentBuilder.field(anyString(), captor.capture())).thenReturn(formContentBuilder);
      when(formContentBuilder.build()).thenReturn(request);
      when(httpClient.post(anyString())).thenReturn(request);
    }

    @Test
    void shouldPostRequest() throws JsonProcessingException {
      String jenkinsUrl = mockJenkinsConfig();

      sender.send(REPOSITORY, new JenkinsRepositoryEventDto(EventTarget.SOURCE, Collections.singletonList(new ProtocolResolverTest.DummyScmProtocol())));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = new ObjectMapper().readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
    }

    @Test
    void shouldPostRequestWithAdditionalServerInformation() throws JsonProcessingException {
      String jenkinsUrl = mockJenkinsConfig();
      serverIdentifications.add(new AdditionalServerIdentification() {
        @Override
        public Identification get() {
          return new Identification("ssh", "hog:2222");
        }
      });

      sender.send(REPOSITORY, new JenkinsRepositoryEventDto(EventTarget.SOURCE, Collections.singletonList(new ProtocolResolverTest.DummyScmProtocol())));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = new ObjectMapper().readTree(captor.getValue());
      assertThat(dto.get("identifications")).hasToString("[{\"name\":\"ssh\",\"value\":\"hog:2222\"}]");
    }

    String mockJenkinsConfig() {
      String jenkinsUrl = "http://hitchhiker.org/";
      when(jenkinsContext.getServerUrl(REPOSITORY)).thenReturn(Optional.of(jenkinsUrl));
      GlobalJenkinsConfiguration globalJenkinsConfiguration = new GlobalJenkinsConfiguration();
      when(jenkinsContext.getConfiguration()).thenReturn(globalJenkinsConfiguration);
      return jenkinsUrl;
    }
  }
}
