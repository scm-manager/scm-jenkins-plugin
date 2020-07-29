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
