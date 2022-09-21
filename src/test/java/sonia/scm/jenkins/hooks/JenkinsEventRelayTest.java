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
import sonia.scm.jenkins.AdditionalServerIdentification;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.jenkins.JenkinsContext;
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
import static sonia.scm.jenkins.hooks.JenkinsEventRelay.EVENT_ENDPOINT;

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
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpResponse response;
  @Mock
  private FormContentBuilder formContentBuilder;

  private final Set<AdditionalServerIdentification> serverIdentifications = new HashSet<>();

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
      serverIdentifications.add(() -> new AdditionalServerIdentification.Identification("ssh", "hog:2222"));

      sender.send(REPOSITORY, new JenkinsRepositoryEventDto(EventTarget.SOURCE, Collections.singletonList(new ProtocolResolverTest.DummyScmProtocol())));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = new ObjectMapper().readTree(captor.getValue());
      assertThat(dto.get("identifications")).hasToString("[{\"name\":\"ssh\",\"value\":\"hog:2222\"}]");
    }

    String mockJenkinsConfig() {
      String jenkinsUrl = "http://hitchhiker.org/";
      when(jenkinsContext.getServerUrl(REPOSITORY)).thenReturn(Optional.of(jenkinsUrl));
      JenkinsConfiguration jenkinsConfiguration = new JenkinsConfiguration();
      jenkinsConfiguration.setUrl("jenkins.org");
      when(jenkinsContext.getConfiguration(REPOSITORY)).thenReturn(jenkinsConfiguration);
      when(jenkinsContext.getConfiguration()).thenReturn(new GlobalJenkinsConfiguration());
      return jenkinsUrl;
    }
  }
}
