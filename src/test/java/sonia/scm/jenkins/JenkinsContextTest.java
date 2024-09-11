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
