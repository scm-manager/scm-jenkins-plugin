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
package sonia.scm.jenkins.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.jenkins.update.JenkinsV2GlobalConfigMigrationUpdateStep.V1JenkinsGlobalConfiguration;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JenkinsV2GlobalConfigMigrationUpdateStepTest {

  private JenkinsV2GlobalConfigMigrationUpdateStep updateStep;
  private static String STORE_NAME = "jenkins";

  private InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  @BeforeEach
  void initUpdateStep() {
    updateStep = new JenkinsV2GlobalConfigMigrationUpdateStep(storeFactory);
  }

  @Nested
  class WithExistingV1Config {

    @BeforeEach
    void createJenkinsV1XMLInMemory() {
      V1JenkinsGlobalConfiguration jenkinsGlobalConfiguration = new V1JenkinsGlobalConfiguration();
      jenkinsGlobalConfiguration.setUrl("jenkins-test.org");

      storeFactory.withType(V1JenkinsGlobalConfiguration.class).withName(STORE_NAME).build().set(jenkinsGlobalConfiguration);
    }

    @Test
    void shouldMigrateGlobalConfigurationWithoutFlags() {
      updateStep.doUpdate();
      ConfigurationStore<GlobalJenkinsConfiguration> testStore = storeFactory.get(STORE_NAME, null);
      GlobalJenkinsConfiguration jenkinsGlobalConfiguration = testStore.get();
      assertThat(jenkinsGlobalConfiguration.getUrl()).isEqualToIgnoringCase("jenkins-test.org");
      assertThat(jenkinsGlobalConfiguration.isDisableGitTrigger()).isFalse();
      assertThat(jenkinsGlobalConfiguration.isDisableMercurialTrigger()).isFalse();
      assertThat(jenkinsGlobalConfiguration.isDisableRepositoryConfiguration()).isFalse();
    }

    @Test
    void shouldMigrateGlobalConfigurationWithFlags() {
      GlobalJenkinsConfiguration jenkinsGlobalConfiguration = (GlobalJenkinsConfiguration) storeFactory.get(STORE_NAME, null).get();
      jenkinsGlobalConfiguration.setDisableGitTrigger(true);
      jenkinsGlobalConfiguration.setDisableMercurialTrigger(true);
      jenkinsGlobalConfiguration.setDisableRepositoryConfiguration(true);
      storeFactory.get(STORE_NAME, null).set(jenkinsGlobalConfiguration);

      updateStep.doUpdate();
      assertThat(jenkinsGlobalConfiguration.getUrl()).isEqualToIgnoringCase("jenkins-test.org");
      assertThat(jenkinsGlobalConfiguration.isDisableGitTrigger()).isTrue();
      assertThat(jenkinsGlobalConfiguration.isDisableMercurialTrigger()).isTrue();
      assertThat(jenkinsGlobalConfiguration.isDisableRepositoryConfiguration()).isTrue();
    }
  }

  @Nested
  class WithExistingV2Config {
    @BeforeEach
    void createJenkinsV2XMLInMemory() {
      GlobalJenkinsConfiguration globalConfiguration = new GlobalJenkinsConfiguration();
      storeFactory.withType(GlobalJenkinsConfiguration.class).withName(STORE_NAME).build().set(globalConfiguration);
    }

    @Test
    void shouldNotFailForExistingV2Config() {
      updateStep.doUpdate();
    }
  }

  @Nested
  class WithoutAnyConfig {
    @BeforeEach
    void createJenkinsV2XMLInMemory() {
    }

    @Test
    void shouldNotFailForMissingConfig() {
      updateStep.doUpdate();
    }
  }
}
