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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsPullRequestEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private JenkinsContext jenkinsContext;
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

  @Captor
  private ArgumentCaptor<JenkinsEventRelay.JenkinsEventDto> captor;

  @InjectMocks
  private JenkinsPullRequestEventRelay eventRelay;

  @Test
  void shouldNotSendIfNotPostEvent() {
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.BEFORE_CREATE));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldNotSendIfEventTriggerDisabled() {
    mockRepo();
    GlobalJenkinsConfiguration configuration = new GlobalJenkinsConfiguration();
    configuration.setDisableEventTrigger(true);
    when(jenkinsContext.getConfiguration()).thenReturn(configuration);
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.CREATE));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldSendForGlobalConfig() throws IOException {
    mockRequest();
    mockRepo();
    String jenkinsUrl = mockJenkinsConfig(true);
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.CREATE));

    verify(httpClient).post(jenkinsUrl);

    JenkinsPullRequestEventRelay.JenkinsEventDto dto = (JenkinsPullRequestEventRelay.JenkinsEventDto) captor.getValue();
    assertThat(dto.getLinks().getLinkBy("dummy")).isPresent();
  }

  @Test
  void shouldSendForRepositoryConfig() throws IOException {
    mockRequest();
    mockRepo();
    String jenkinsUrl = mockJenkinsConfig(false);
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.CREATE));

    verify(httpClient).post(jenkinsUrl);

    JenkinsEventRelay.JenkinsEventDto dto = captor.getValue();
    assertThat(dto.getLinks().getLinkBy("dummy").isPresent()).isTrue();
  }

  private String mockJenkinsConfig(boolean disableRepoConfig) {
    String jenkinsUrl = "http://hitchhiker.org/";
    GlobalJenkinsConfiguration globalJenkinsConfiguration = new GlobalJenkinsConfiguration();
    globalJenkinsConfiguration.setDisableRepositoryConfiguration(disableRepoConfig);
    globalJenkinsConfiguration.setUrl(jenkinsUrl);
    JenkinsConfiguration jenkinsConfiguration = new JenkinsConfiguration();
    jenkinsConfiguration.setUrl(jenkinsUrl);
    lenient().when(jenkinsContext.getConfiguration()).thenReturn(globalJenkinsConfiguration);
    lenient().when(jenkinsContext.getConfiguration(REPOSITORY)).thenReturn(jenkinsConfiguration);

    return jenkinsUrl;
  }

  private void mockRequest() throws IOException {
    when(request.request()).thenReturn(response);
    when(httpClient.post(anyString())).thenReturn(request);
    when(request.jsonContent(captor.capture())).thenReturn(request);
  }

  private void mockRepo() {
    List<ScmProtocol> protocols = new ArrayList<>();
    protocols.add(new DummyScmProtocol());
    when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getSupportedProtocols()).thenReturn(protocols.stream());
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
