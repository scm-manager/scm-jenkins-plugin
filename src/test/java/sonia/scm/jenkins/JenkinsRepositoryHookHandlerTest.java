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

import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.ContentTransformer;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookContext;

import javax.inject.Provider;
import javax.ws.rs.core.MediaType;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class JenkinsRepositoryHookHandlerTest {

  private JenkinsConfiguration configuration;
  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
  @Mock
  private AdvancedHttpClient advancedHttpClient;
  @Mock
  private AdvancedHttpRequestWithBody request;
  @Mock
  private AdvancedHttpResponse response;
  @Mock
  private HookContext hookContext;

  private JenkinsRepositoryHookHandler handler;

  @BeforeEach
  void setUp() {
    configuration = new JenkinsConfiguration();
    configuration.setUrl("http://hitchhiker.org/jenkins");
    configuration.setProject("HeartOfGold");
    handler = new JenkinsRepositoryHookHandler(httpClientProvider,
      configuration);
  }

  @Test
  void testEscape() {
    assertThat(handler.escape("https://ci.scm-manager.org")).isEqualTo("https://ci.scm-manager.org");
    assertThat(handler.escape("https://ci.scm-manager.org/path")).isEqualTo("https://ci.scm-manager.org/path");
    assertThat(handler.escape("https://ci.scm-manager.org/some/deep/path")).isEqualTo("https://ci.scm-manager.org/some/deep/path");
    assertThat(handler.escape("https://ci.scm-manager.org/path?with=query&param=true")).isEqualTo("https://ci.scm-manager.org/path?with=query&param=true");
    assertThat(handler.escape("https://ci.scm-manager.org/with spaces")).isEqualTo("https://ci.scm-manager.org/with%20spaces");
  }

  @Test
  void shouldSendWithoutBuildParameters() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(configuration.getUrl() + "/job/" + configuration.getProject() + "/build");
  }

  @Test
  void shouldSendWithSingleBuildParameter() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    configuration.addBuildParameter("author", "trillian");

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(configuration.getUrl() + "/job/" + configuration.getProject() + "/buildWithParameters?author=trillian");
  }

  @Test
  void shouldSendWithMultipleBuildParameters() throws IOException {
    when(httpClientProvider.get()).thenReturn(advancedHttpClient);
    when(request.request()).thenReturn(response);
    when(response.getStatus()).thenReturn(200);
    when(advancedHttpClient.post(anyString())).thenReturn(request);
    configuration.addBuildParameter("author", "trillian");
    configuration.addBuildParameter("version", "42");
    configuration.addBuildParameter("environment", "Betelgeuse");

    Repository repository = RepositoryTestData.createHeartOfGold();
    handler.sendRequest(new RepositoryHookEvent(hookContext, repository, RepositoryHookType.POST_RECEIVE));

    verify(advancedHttpClient).post(configuration.getUrl() + "/job/" + configuration.getProject() + "/buildWithParameters?author=trillian&version=42&environment=Betelgeuse");
  }
}
