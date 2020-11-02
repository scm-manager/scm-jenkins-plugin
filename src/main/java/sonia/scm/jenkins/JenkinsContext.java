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
