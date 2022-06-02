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
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Added;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Copied;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Person;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.LookupCommandBuilder;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsSvnGlobalHookHandlerTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private LookupCommandBuilder lookupCommand;
  @Mock(answer = Answers.RETURNS_SELF)
  private ModificationsCommandBuilder modificationsCommand;
  @Mock
  private GlobalJenkinsConfiguration config;
  @Mock
  private AdvancedHttpClient advancedHttpClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequestWithBody request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext hookContext;
  @Mock
  private HookChangesetBuilder changesetProvider;

  private JenkinsSvnGlobalHookHandler handler;

  @BeforeEach
  void initClient() {
    Provider<AdvancedHttpClient> httpClientProvider = Providers.of(advancedHttpClient);
    handler = new JenkinsSvnGlobalHookHandler(httpClientProvider, config, serviceFactory);
  }

  @Test
  void shouldNotSendRequestIfTriggerIsDisabled() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(config.isDisableSubversionTrigger()).thenReturn(true);
    when(config.getUrl()).thenReturn("http://jenkins.io/scm/");

    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient, never()).post(anyString());
  }

  @Test
  void shouldNotSendRequestIfUrlIsNotSet() {
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(config.getUrl()).thenReturn("");

    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient, never()).post(anyString());
  }

  @Test
  void shouldSendRequestIfTriggerIsEnabledWithCsrfCrumb() throws IOException {
    ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(config.isDisableSubversionTrigger()).thenReturn(false);
    when(config.getUrl()).thenReturn("http://jenkins.io/scm/");
    when(config.getUsername()).thenReturn("Trillian");
    when(config.getApiToken()).thenReturn("Secret");
    mockHttpClient(repository, contentCaptor);
    mockChangesetProvider();
    mockModifications();
    mockCrumbRequester();

    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post("http://jenkins.io/scm/subversion/uuid-42/notifyCommit/?rev=1");
    String value = contentCaptor.getValue();
    assertThat(value)
      .contains("A   .gitignore")
      .contains("A   copy.dog")
      .contains("U   Jenkinsfile")
      .contains("U   readme.md")
      .contains("D   pom.xml");
  }

  @Test
  void shouldSendRequestIfTriggerIsEnabledWithoutCsrfCrumb() throws IOException {
    ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
    Repository repository = RepositoryTestData.createHeartOfGold();
    when(config.isDisableSubversionTrigger()).thenReturn(false);
    when(config.getUrl()).thenReturn("http://jenkins.io/scm/");
    when(config.getUsername()).thenReturn(null);
    mockHttpClient(repository, contentCaptor);
    mockChangesetProvider();
    mockModifications();

    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post("http://jenkins.io/scm/subversion/uuid-42/notifyCommit/?rev=1");
    String value = contentCaptor.getValue();
    assertThat(value)
      .contains("A   .gitignore")
      .contains("A   copy.dog")
      .contains("U   Jenkinsfile")
      .contains("U   readme.md")
      .contains("D   pom.xml");
  }

  private void mockModifications() throws IOException {
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommand);
    ImmutableList<Modification> modifications = ImmutableList.of(
      new Added(".gitignore"),
      new Modified("Jenkinsfile"),
      new Renamed("README.md", "readme.md"),
      new Removed("pom.xml"),
      new Copied("copy.cat", "copy.dog")
    );
    when(modificationsCommand.getModifications()).thenReturn(new Modifications("1", modifications));
  }

  private void mockChangesetProvider() {
    when(hookContext.getChangesetProvider()).thenReturn(changesetProvider);
    when(changesetProvider.getChangesetList()).thenReturn(ImmutableList.of(new Changeset("1", 0L, Person.toPerson("trillian"))));
  }

  private void mockHttpClient(Repository repository, ArgumentCaptor<String> contentCaptor) throws IOException {
    when(serviceFactory.create(repository)).thenReturn(repositoryService);
    when(repositoryService.getLookupCommand()).thenReturn(lookupCommand);
    when(lookupCommand.lookup(String.class, "propget", "uuid", "/")).thenReturn(Optional.of("uuid-42"));
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    when(request.spanKind("Jenkins")).thenReturn(request);
    when(request.stringContent(contentCaptor.capture())).thenReturn(request);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
  }

  private void mockCrumbRequester() throws IOException {
    AdvancedHttpRequest getRequest = mock(AdvancedHttpRequest.class, RETURNS_SELF);
    AdvancedHttpResponse crumbResponse = mock(AdvancedHttpResponse.class);
    when(advancedHttpClient.get(anyString())).thenReturn(getRequest);
    when(getRequest.request()).thenReturn(crumbResponse);
    when(crumbResponse.getStatus()).thenReturn(200);
    when(crumbResponse.contentAsStream()).thenReturn(new ByteArrayInputStream("crumb".getBytes()));
  }
}
