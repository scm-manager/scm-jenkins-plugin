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

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class JenkinsGlobalHookHandlerTest {

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

  @Nested
  class ForHg {
    private JenkinsGlobalHookHandler handler;

    @BeforeEach
    void initClient() {
      Provider<AdvancedHttpClient> httpClientProvider = Providers.of(advancedHttpClient);
      handler = new JenkinsHgGlobalHookHandler(httpClientProvider, config, serviceFactory);
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
      void shouldNotSendRequestForMercurialRepoIfTriggerIsDisabled() {
        when(config.isDisableMercurialTrigger()).thenReturn(true);
        Repository repository = RepositoryTestData.createHeartOfGold();
        repository.setType("hg");

        handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

        verify(advancedHttpClient, never()).get(anyString());
      }

      @Test
      void shouldSendRequestForMercurialRepoIfTriggerEnabled() throws IOException {
        Repository repository = RepositoryTestData.createHeartOfGold();
        repository.setType("hg");

        when(config.isDisableMercurialTrigger()).thenReturn(false);
        when(serviceFactory.create(repository)).thenReturn(repositoryService);
        mockHttpClient(repository);

        handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

        verify(advancedHttpClient).get("jenkins.io/scm/mercurial/notifyCommit");
      }
    }
  }

  @Nested
  class ForGit {
    private JenkinsGlobalHookHandler handler;

    @BeforeEach
    void initClient() {
      Provider<AdvancedHttpClient> httpClientProvider = Providers.of(advancedHttpClient);
      handler = new JenkinsGitGlobalHookHandler(httpClientProvider, config, serviceFactory);
    }

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
  }

  private void mockHttpClient(Repository repository) throws IOException {
    when(serviceFactory.create(repository)).thenReturn(repositoryService);
    when(advancedHttpClient.get(anyString())).thenReturn(request);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
  }
}
