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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Optional;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class JenkinsContext {

  public static final String NAME = "jenkins";

  @Inject
  public JenkinsContext(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void storeConfiguration(GlobalJenkinsConfiguration configuration) {
    createGlobalStore().set(configuration);
  }

  public void storeConfiguration(JenkinsConfiguration configuration, Repository repository) {
    createStore(repository).set(configuration);
  }

  public GlobalJenkinsConfiguration getConfiguration() {
    return createGlobalStore().getOptional().orElse(new GlobalJenkinsConfiguration());
  }

  public JenkinsConfiguration getConfiguration(Repository repository) {
    return createStore(repository).getOptional().orElse(new JenkinsConfiguration());
  }

  private ConfigurationStore<JenkinsConfiguration> createStore(Repository repository) {
    return storeFactory.withType(JenkinsConfiguration.class).withName(NAME).forRepository(repository).build();
  }

  private ConfigurationStore<GlobalJenkinsConfiguration> createGlobalStore() {
    return storeFactory.withType(GlobalJenkinsConfiguration.class).withName(NAME).build();
  }

  public Optional<String> getServerUrl(Repository repository) {
    GlobalJenkinsConfiguration globalConfig = getConfiguration();
    if (!globalConfig.isDisableRepositoryConfiguration()) {
      JenkinsConfiguration repoConfig = getConfiguration(repository);
      if (!Strings.isNullOrEmpty(repoConfig.getUrl())) {
        return Optional.of(repoConfig.getUrl());
      }
    }

    if (globalConfig.isValid()) {
      return Optional.of(globalConfig.getUrl());
    }
    return Optional.empty();
  }

  public Optional<String> getServerUrl() {
    GlobalJenkinsConfiguration globalConfig = getConfiguration();
    if (globalConfig.isValid()) {
      return Optional.of(globalConfig.getUrl());
    }
    return Optional.empty();
  }

  private ConfigurationStoreFactory storeFactory;
}
