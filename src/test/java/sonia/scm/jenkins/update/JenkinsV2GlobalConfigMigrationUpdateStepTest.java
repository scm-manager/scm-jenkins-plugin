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
