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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.jenkins.JenkinsConfigurationStoreFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class JenkinsV2ConfigMigrationUpdateStep implements UpdateStep {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsV2ConfigMigrationUpdateStep.class);

    private final V1PropertyDAO v1PropertyDAO;
    private final JenkinsConfigurationStoreFactory configStoreFactory;

    private static final String JENKINS_API_TOKEN = "jenkins.api-token";
    private static final String JENKINS_BRANCHES = "jenkins.branches";
    private static final String JENKINS_PROJECT = "jenkins.project";
    private static final String JENKINS_TOKEN = "jenkins.token";
    private static final String JENKINS_URL = "jenkins.url";
    private static final String JENKINS_USERNAME = "jenkins.username";
    private static final String JENKINS_CSRF = "jenkins.csrf";

    @Inject
    public JenkinsV2ConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, JenkinsConfigurationStoreFactory configStoreFactory) {
      this.v1PropertyDAO = v1PropertyDAO;
      this.configStoreFactory = configStoreFactory;
    }

    @Override
    public void doUpdate() {
      v1PropertyDAO
        .getProperties(REPOSITORY_PROPERTY_READER)
        .havingAnyOf(JENKINS_API_TOKEN, JENKINS_BRANCHES, JENKINS_PROJECT, JENKINS_TOKEN, JENKINS_URL, JENKINS_USERNAME, JENKINS_CSRF)
        .forEachEntry((key, properties) -> configStoreFactory.storeConfiguration(buildConfig(key, properties), key));
    }

    private JenkinsConfiguration buildConfig(String repositoryId, V1Properties properties) {
      LOG.debug("migrating repository specific jenkins configuration for repository id {}", repositoryId);
      Set<String> branches= new HashSet<>();
      if(!properties.get(JENKINS_BRANCHES).isEmpty()) {
        branches.addAll(Arrays.asList(properties.get(JENKINS_BRANCHES).split(",")));
      }

      JenkinsConfiguration configuration = new JenkinsConfiguration();
      configuration.setApiToken(properties.get(JENKINS_API_TOKEN));
      configuration.setBranches(Collections.singleton(properties.get(JENKINS_BRANCHES)));
      configuration.setProject(properties.get(JENKINS_PROJECT));
      configuration.setToken(properties.get(JENKINS_TOKEN));
      configuration.setUrl(properties.get(JENKINS_URL));
      configuration.setUsername(properties.get(JENKINS_USERNAME));
      configuration.setBranches(branches);
      configuration.setCsrf(properties.getBoolean(JENKINS_CSRF).orElse(false));
      return configuration;
    }

    @Override
    public Version getTargetVersion() {
      return parse("2.0.0");
    }

    @Override
    public String getAffectedDataType() {
      return "sonia.scm.jenkins.config.repository.xml";
    }
  }


