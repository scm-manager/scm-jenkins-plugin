/**
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
