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
package sonia.scm.jenkins.update;

import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.migration.UpdateStep;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

import static sonia.scm.version.Version.parse;

public class JenkinsV2GlobalConfigMigrationUpdateStep implements UpdateStep {

    public static final String STORE_NAME = "jenkins";
    private final ConfigurationStoreFactory storeFactory;

  @Inject
  public JenkinsV2GlobalConfigMigrationUpdateStep(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    Optional<V1JenkinsGlobalConfiguration> optionalConfig = storeFactory.withType(V1JenkinsGlobalConfiguration.class).withName(STORE_NAME).build().getOptional();
    if (isV1Config(optionalConfig)) {
      optionalConfig.ifPresent(
        v1JenkinsGlobalConfiguration -> {
          GlobalJenkinsConfiguration v2JenkinsConfig = new GlobalJenkinsConfiguration(
            v1JenkinsGlobalConfiguration.getUrl(),
            v1JenkinsGlobalConfiguration.isDisableGitTrigger(),
            v1JenkinsGlobalConfiguration.isDisableMercurialTrigger(),
            false,
            v1JenkinsGlobalConfiguration.isDisableRepositoryConfiguration()
          );
          storeFactory.withType(GlobalJenkinsConfiguration.class).withName(STORE_NAME).build().set(v2JenkinsConfig);
        }
      );
    }
  }

  private boolean isV1Config(Optional<V1JenkinsGlobalConfiguration> optionalConfig) {
    if (optionalConfig.isPresent()) {
      try {
        return optionalConfig.get() instanceof V1JenkinsGlobalConfiguration;
      } catch (ClassCastException e) {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.jenkins.config.global.xml";
  }

  @XmlRootElement(name = "jenkins-config")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class V1JenkinsGlobalConfiguration extends GlobalJenkinsConfiguration {
  }
}
