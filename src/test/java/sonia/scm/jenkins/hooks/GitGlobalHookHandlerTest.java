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
import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.jenkins.JenkinsContext;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitGlobalHookHandlerTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private GlobalJenkinsConfiguration config;
  @Mock
  private AdvancedHttpClient advancedHttpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequest request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext hookContext;

  private GitGlobalHookHandler handler;

  @BeforeEach
  void initClient() {
    Provider<AdvancedHttpClient> httpClientProvider = Providers.of(advancedHttpClient);
    handler = new GitGlobalHookHandler(httpClientProvider, serviceFactory, config);
  }

  @Test
  void shouldNotSendRequestIfConfigInvalid() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(config.isValid()).thenReturn(false);

    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient, never()).get(anyString());
  }

  @Nested
  class WithValidConfig {
    @BeforeEach
    void initValidConfig() {
      when(config.isValid()).thenReturn(true);
      lenient().when(config.getUrl()).thenReturn("jenkins.io/scm/");
    }

    @Test
    void shouldNotSendRequestForGitRepoIfTriggerIsDisabled() {
      when(config.isDisableGitTrigger()).thenReturn(true);
      Repository repository = RepositoryTestData.createHeartOfGold();
      repository.setType("git");

      handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

      verify(advancedHttpClient, never()).get(anyString());
    }

    @Test
    void shouldSendRequestForGitRepoIfTriggerIsEnabled() throws IOException {
      Repository repository = RepositoryTestData.createHeartOfGold();
      repository.setType("git");
      when(config.isDisableGitTrigger()).thenReturn(false);
      mockHttpClient(repository);

      handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

      verify(advancedHttpClient).get("jenkins.io/scm/git/notifyCommit");
    }

    private void mockHttpClient(Repository repository) throws IOException {
      when(serviceFactory.create(repository)).thenReturn(repositoryService);
      when(advancedHttpClient.get(anyString())).thenReturn(request);
      when(request.request()).thenReturn(response);
      when(response.getStatus()).thenReturn(200);
    }
  }
}
