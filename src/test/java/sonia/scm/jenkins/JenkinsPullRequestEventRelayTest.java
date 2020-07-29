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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestUpdatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.FormContentBuilder;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent.RejectionCause.REJECTED_BY_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.jenkins.JenkinsEventRelay.EVENT_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class JenkinsPullRequestEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final PullRequest PULL_REQUEST = new PullRequest("42", "feature", "master");

  private static final String SERVER_URL = "http://scm-manager.org";
  private final ObjectMapper objectMapper = new ObjectMapper();


  @Mock
  private JenkinsContext jenkinsContext;
  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private RepositoryServiceFactory repositoryServiceFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryService;
  @Mock
  private AdvancedHttpClient httpClient;
  @Mock
  private AdvancedHttpRequestWithBody request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock
  private FormContentBuilder formContentBuilder;
  @Mock
  private ScmConfiguration configuration;

  @Captor
  private ArgumentCaptor<String> captor;

  @InjectMocks
  private JenkinsPullRequestEventRelay eventRelay;

  @BeforeEach
  void initScmConfig() {
    lenient().when(scmConfiguration.getBaseUrl()).thenReturn(SERVER_URL);
  }

  @BeforeEach
  void mockRepositoryProtocols() {
    List<ScmProtocol> protocols = new ArrayList<>();
    protocols.add(new DummyScmProtocol());
    lenient().when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    lenient().when(repositoryService.getSupportedProtocols()).thenReturn(protocols.stream());
  }

  @ParameterizedTest
  @ValueSource(strings = {"BEFORE_CREATE", "BEFORE_MODIFY", "BEFORE_DELETE", "MODIFY"})
  void shouldNotSendForIrrelevantEvents(String eventName) {
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.valueOf(eventName)));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldNotSendIfEventTriggerDisabled() {
    GlobalJenkinsConfiguration configuration = new GlobalJenkinsConfiguration();
    configuration.setDisableEventTrigger(true);
    when(jenkinsContext.getConfiguration()).thenReturn(configuration);
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.CREATE));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldNotSendWithoutValidConfiguration() {
    when(jenkinsContext.getConfiguration()).thenReturn(new GlobalJenkinsConfiguration());
    when(jenkinsContext.getServerUrl(REPOSITORY)).thenReturn(Optional.empty());

    PullRequest pr = new PullRequest();
    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.CREATE));

    verify(httpClient, never()).post(anyString());
  }


  @Nested
  class WithExpectedRequest {

    @BeforeEach
    void mockRequest() throws IOException {
      when(request.request()).thenReturn(response);
      when(request.formContent()).thenReturn(formContentBuilder);
      when(formContentBuilder.field(anyString(), captor.capture())).thenReturn(formContentBuilder);
      when(formContentBuilder.build()).thenReturn(request);
      when(httpClient.post(anyString())).thenReturn(request);
    }

    @Test
    void shouldSendForGlobalConfig() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handle(new PullRequestEvent(REPOSITORY, PULL_REQUEST, null, HandlerEventType.CREATE));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
    }

    @Test
    void shouldSendForRepositoryConfig() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handle(new PullRequestEvent(REPOSITORY, PULL_REQUEST, null, HandlerEventType.CREATE));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
    }

    @Test
    void shouldSendForCreateEvent() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handle(new PullRequestEvent(REPOSITORY, PULL_REQUEST, null, HandlerEventType.CREATE));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("id")).hasToString("\"42\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("source")).hasToString("\"feature\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("target")).hasToString("\"master\"");
    }

    @Test
    void shouldSendForUpdatedEvent() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handleUpdatedEvent(new PullRequestUpdatedEvent(REPOSITORY, PULL_REQUEST));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("id")).hasToString("\"42\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("source")).hasToString("\"feature\"");
      assertThat(dto.get("createOrModifiedPullRequests").get(0).get("target")).hasToString("\"master\"");
    }

    @Test
    void shouldSendForMergedEvent() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handleMergedEvent(new PullRequestMergedEvent(REPOSITORY, PULL_REQUEST));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("id")).hasToString("\"42\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("source")).hasToString("\"feature\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("target")).hasToString("\"master\"");
    }

    @Test
    void shouldSendForRejectedEvent() throws IOException {
      String jenkinsUrl = mockJenkinsConfig();

      eventRelay.handleRejectedEvent(new PullRequestRejectedEvent(REPOSITORY, PULL_REQUEST, REJECTED_BY_USER));

      verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

      JsonNode dto = objectMapper.readTree(captor.getValue());
      assertThat(dto.has("_links")).isTrue();
      assertThat(dto.get("_links").has("dummy")).isTrue();
      assertThat(dto.get("namespace")).hasToString("\"" + REPOSITORY.getNamespace() + "\"");
      assertThat(dto.get("name")).hasToString("\"" + REPOSITORY.getName() + "\"");
      assertThat(dto.get("type")).hasToString("\"" + REPOSITORY.getType() + "\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("id")).hasToString("\"42\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("source")).hasToString("\"feature\"");
      assertThat(dto.get("deletedPullRequests").get(0).get("target")).hasToString("\"master\"");
    }

    String mockJenkinsConfig() {
      String jenkinsUrl = "http://hitchhiker.org/";
      when(jenkinsContext.getServerUrl(REPOSITORY)).thenReturn(Optional.of(jenkinsUrl));
      GlobalJenkinsConfiguration globalJenkinsConfiguration = new GlobalJenkinsConfiguration();
      when(jenkinsContext.getConfiguration()).thenReturn(globalJenkinsConfiguration);
      return jenkinsUrl;
    }
  }

  public static class DummyScmProtocol implements ScmProtocol {

    @Override
    public String getType() {
      return "dummy";
    }

    @Override
    public String getUrl() {
      return "dummyUrl";
    }
  }
}
