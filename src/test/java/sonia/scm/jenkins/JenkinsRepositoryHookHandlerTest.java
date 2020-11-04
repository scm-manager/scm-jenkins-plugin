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

import com.cloudogu.scm.el.ElParser;
import com.cloudogu.scm.el.jexl.JexlParser;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookContext;
import sonia.scm.util.HttpUtil;

import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsRepositoryHookHandlerTest {

  private JenkinsConfiguration config;
  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
  @Mock
  private AdvancedHttpClient advancedHttpClient;
  @Mock
  private AdvancedHttpRequestWithBody request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext hookContext;

  private final ElParser elParser = new JexlParser();

  private JenkinsRepositoryHookHandler handler;

  @BeforeEach
  void setUp() {
    config = new JenkinsConfiguration();
    config.setUrl("http://hitchhiker.org/jenkins");
    config.setProject("HeartOfGold");
    handler = new JenkinsRepositoryHookHandler(httpClientProvider, config, elParser);
  }

  @Test
  void shouldSendWithoutBuildParameters() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    when(hookContext.getChangesetProvider().getChangesets()).thenReturn(null);

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(config.getUrl() + "/job/" + config.getProject() + "/build");
  }

  @Test
  void shouldSendWithSingleBuildParameter() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    when(hookContext.getChangesetProvider().getChangesets()).thenReturn(null);
    config.setBuildParameters(ImmutableSet.of(
      new BuildParameter("author", "trillian @hitchhiker/42")
    ));

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(config.getUrl() + "/job/" + config.getProject() + "/buildWithParameters?author=trillian+%40hitchhiker%2F42");
  }

  @Test
  void shouldSendWithMultipleBuildParameters() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    when(hookContext.getChangesetProvider().getChangesets()).thenReturn(null);

    config.setBuildParameters(ImmutableSet.of(
      new BuildParameter("author", "trillian"),
      new BuildParameter("version", "42"),
      new BuildParameter("environment", "Betelgeuse")
    ));

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(config.getUrl() + "/job/" + config.getProject() + "/buildWithParameters?author=trillian&version=42&environment=Betelgeuse");
  }

  @Test
  void shouldSendWithVariables() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    ArrayList<Changeset> changesets = new ArrayList<>();
    Changeset changeset = new Changeset("1", 1L, new Person("trillian", "trillian@hitchhiker.org"));
    changesets.add(changeset);
    when(hookContext.getChangesetProvider().getChangesets()).thenReturn(changesets);

    config.setBuildParameters(ImmutableSet.of(
      new BuildParameter("author", "${changeset.author.name}"),
      new BuildParameter("commitId", "${commit.id}"),
      new BuildParameter("repoName", "${repository.name}"),
      new BuildParameter("mail", "${commit.author.mail}")
    ));

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(
      config.getUrl()
        + "/job/"
        + config.getProject()
        + "/buildWithParameters?author="
        + changeset.getAuthor().getName()
        + "&commitId="
        + changeset.getId()
        + "&repoName="
        + repository.getName()
        + "&mail="
        + HttpUtil.encode(changeset.getAuthor().getMail())
    );
  }
}
