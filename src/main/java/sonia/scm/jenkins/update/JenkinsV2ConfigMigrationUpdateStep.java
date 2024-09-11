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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.jenkins.JenkinsConfigurationStoreFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import jakarta.inject.Inject;

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
      if (!Strings.isNullOrEmpty(properties.get(JENKINS_BRANCHES))) {
        branches.addAll(Arrays.asList(properties.get(JENKINS_BRANCHES).split(",")));
      }

      JenkinsConfiguration configuration = new JenkinsConfiguration();
      configuration.setApiToken(properties.get(JENKINS_API_TOKEN));
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


