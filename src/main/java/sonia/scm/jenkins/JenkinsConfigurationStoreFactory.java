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

import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;

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
