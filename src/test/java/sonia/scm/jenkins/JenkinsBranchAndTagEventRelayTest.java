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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
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
import static sonia.scm.jenkins.JenkinsEventRelay.EVENT_ENDPOINT;

@ExtendWith(MockitoExtension.class)
class JenkinsBranchAndTagEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private JenkinsContext jenkinsContext;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext hookContext;
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
  private JenkinsBranchAndTagEventRelay eventRelay;

  @Test
  void shouldNotSend() {
    mockRepo();
    mockJenkinsConfig(false);

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(httpClient, never()).post(anyString());
  }

  @Test
  void shouldSendIfBranchesAvailable() throws IOException {
    mockRepo();
    mockRequest();

    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(ImmutableList.of("master"));
    when(hookContext.getBranchProvider().getDeletedOrClosed()).thenReturn(ImmutableList.of("develop"));

    String jenkinsUrl = mockJenkinsConfig(false);

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

    JenkinsBranchAndTagEventRelay.JenkinsEventDto eventDto = (JenkinsBranchAndTagEventRelay.JenkinsEventDto) captor.getValue();
    assertThat((eventDto).getCreatedOrModifiedBranches().get(0)).isEqualTo("master");
    assertThat((eventDto).getDeletedBranches().get(0)).isEqualTo("develop");
  }

  @Test
  void shouldSendIfTagsAvailable() throws IOException {
    mockRepo();
    mockRequest();
    String jenkinsUrl = mockJenkinsConfig(false);

    when(hookContext.isFeatureSupported(HookFeature.TAG_PROVIDER)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(ImmutableList.of(new Tag("snapshot", "1")));
    when(hookContext.getTagProvider().getDeletedTags()).thenReturn(ImmutableList.of(new Tag("release", "2")));

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

    JenkinsBranchAndTagEventRelay.JenkinsEventDto eventDto = (JenkinsBranchAndTagEventRelay.JenkinsEventDto) captor.getValue();
    assertThat((eventDto).getCreateOrModifiedTags().get(0).getName()).isEqualTo("snapshot");
    assertThat((eventDto).getDeletedTags().get(0).getName()).isEqualTo("release");
  }

  @Test
  void shouldSendIfBranchesAndTagsAreAvailable() throws IOException {
    mockRepo();
    mockRequest();
    String jenkinsUrl = mockJenkinsConfig(true);

    when(hookContext.isFeatureSupported(HookFeature.TAG_PROVIDER)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(ImmutableList.of(new Tag("snapshot", "1")));
    when(hookContext.getTagProvider().getDeletedTags()).thenReturn(ImmutableList.of(new Tag("release", "2")));
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(ImmutableList.of("master"));
    when(hookContext.getBranchProvider().getDeletedOrClosed()).thenReturn(ImmutableList.of("develop"));

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(httpClient).post(jenkinsUrl + EVENT_ENDPOINT);

    JenkinsBranchAndTagEventRelay.JenkinsEventDto eventDto = (JenkinsBranchAndTagEventRelay.JenkinsEventDto) captor.getValue();
    assertThat((eventDto).getCreateOrModifiedTags().get(0).getName()).isEqualTo("snapshot");
    assertThat((eventDto).getDeletedTags().get(0).getName()).isEqualTo("release");
    assertThat((eventDto).getCreatedOrModifiedBranches().get(0)).isEqualTo("master");
    assertThat((eventDto).getDeletedBranches().get(0)).isEqualTo("develop");
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

  private void mockRepo() {
    when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    List<ScmProtocol> protocols = new ArrayList<>();
    protocols.add(new JenkinsPullRequestEventRelayTest.DummyScmProtocol());
    when(repositoryServiceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getSupportedProtocols()).thenReturn(protocols.stream());
  }

  private void mockRequest() throws IOException {
    when(request.request()).thenReturn(response);
    when(httpClient.post(anyString())).thenReturn(request);
    when(request.jsonContent(captor.capture())).thenReturn(request);
  }
}
