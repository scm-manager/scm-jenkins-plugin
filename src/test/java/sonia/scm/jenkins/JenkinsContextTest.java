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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JenkinsContextTest {

  public static final Repository HEART_OF_GOLD = RepositoryTestData.createHeartOfGold();
  private JenkinsContext context;

  @BeforeEach
  void setUpJenkinsContext() {
    context = new JenkinsContext(new InMemoryConfigurationStoreFactory());
  }

  @Test
  void shouldReturnUrlFromRepositoryConfiguration() {
    setRepositoryUrl("https://hitchhiker.com/scm");

    Optional<String> serverUrl = context.getServerUrl(HEART_OF_GOLD);
    assertThat(serverUrl).contains("https://hitchhiker.com/scm");
  }

  @Test
  void shouldReturnUrlFromGlobalConfiguration() {
    setGlobalUrl("https://hitchhiker.com/scm");

    Optional<String> serverUrl = context.getServerUrl(HEART_OF_GOLD);
    assertThat(serverUrl).contains("https://hitchhiker.com/scm");
  }

  @Test
  void shouldReturnUrlFromRepositoryConfigurationIfBothAreConfigured() {
    setRepositoryUrl("https://hitchhiker.com/repo");
    setGlobalUrl("https://hitchhiker.com/global");

    Optional<String> serverUrl = context.getServerUrl(HEART_OF_GOLD);
    assertThat(serverUrl).contains("https://hitchhiker.com/repo");
  }

  @Test
  void shouldReturnUrlFromGlobalConfigurationIfRepositoryConfigurationIsDisabled() {
    setRepositoryUrl("https://hitchhiker.com/repo");
    setGlobalUrl("https://hitchhiker.com/global", true);

    Optional<String> serverUrl = context.getServerUrl(HEART_OF_GOLD);
    assertThat(serverUrl).contains("https://hitchhiker.com/global");
  }

  @Test
  void shouldReturnEmptyWithoutValidConfiguration() {
    Optional<String> serverUrl = context.getServerUrl(HEART_OF_GOLD);
    assertThat(serverUrl).isEmpty();
  }

  @Test
  void shouldReturnGlobalServerUrl() {
    setGlobalUrl("https://hitchhiker.com/global", true);

    Optional<String> serverUrl = context.getServerUrl();

    assertThat(serverUrl).get().isEqualTo("https://hitchhiker.com/global");
  }

  @Test
  void shouldReturnEmptyServerUrlWhenNotConfigured() {
    Optional<String> serverUrl = context.getServerUrl();
    assertThat(serverUrl).isEmpty();
  }

  private void setGlobalUrl(String url) {
    setGlobalUrl(url, false);
  }

  private void setGlobalUrl(String url, boolean disableRepositoryConfiguration) {
    GlobalJenkinsConfiguration configuration = new GlobalJenkinsConfiguration();
    configuration.setDisableRepositoryConfiguration(disableRepositoryConfiguration);
    configuration.setUrl(url);
    context.storeConfiguration(configuration);
  }

  private void setRepositoryUrl(String url) {
    JenkinsConfiguration configuration = context.getConfiguration(HEART_OF_GOLD);
    configuration.setUrl(url);
    context.storeConfiguration(configuration, HEART_OF_GOLD);
  }

}
