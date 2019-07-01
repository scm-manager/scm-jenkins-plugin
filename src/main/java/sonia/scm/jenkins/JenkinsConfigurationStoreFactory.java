package sonia.scm.jenkins;

import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;

public class JenkinsConfigurationStoreFactory {

  private static final String JENKINS_STORE_NAME = "jenkins";

  private final ConfigurationStoreFactory dataStoreFactory;

  @Inject
  public JenkinsConfigurationStoreFactory(ConfigurationStoreFactory dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  public void storeConfiguration(JenkinsConfiguration configuration, String repositoryId) {
    create(repositoryId).set(configuration);
  }

  private ConfigurationStore<JenkinsConfiguration> create(String repositoryId) {
    return dataStoreFactory.withType(JenkinsConfiguration.class).withName(JENKINS_STORE_NAME).forRepository(repositoryId).build();
  }

  public ConfigurationStore<JenkinsConfiguration> create(Repository repository) {
    return dataStoreFactory.withType(JenkinsConfiguration.class).withName(JENKINS_STORE_NAME).forRepository(repository).build();
  }

  public ConfigurationStore<JenkinsConfiguration> create() {
    return dataStoreFactory.withType(JenkinsConfiguration.class).withName(JENKINS_STORE_NAME).build();
  }
}
