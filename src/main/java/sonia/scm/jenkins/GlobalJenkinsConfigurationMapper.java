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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.jenkins.JenkinsContext.NAME;

@Mapper
public abstract class GlobalJenkinsConfigurationMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @VisibleForTesting
  @SuppressWarnings("squid:S2068") // we have no password here
  static final String DUMMY_SECRET = "__DUMMY__";

  public abstract GlobalJenkinsConfigurationDto map(GlobalJenkinsConfiguration config);

  public abstract GlobalJenkinsConfiguration map(GlobalJenkinsConfigurationDto dto, @Context GlobalJenkinsConfiguration oldConfiguration);

  @AfterMapping
  public void replaceSecretsWithDummy(@MappingTarget GlobalJenkinsConfigurationDto target) {
    if (StringUtils.isNotEmpty(target.getApiToken())) {
      target.setApiToken(DUMMY_SECRET);
    }
    if (StringUtils.isNotEmpty(target.getGitAuthenticationToken())) {
      target.setGitAuthenticationToken(DUMMY_SECRET);
    }
  }

  @AfterMapping
  public void restoreSecretsOnDummy(@MappingTarget GlobalJenkinsConfiguration target, @Context GlobalJenkinsConfiguration oldConfiguration) {
    if (DUMMY_SECRET.equals(target.getApiToken())) {
      target.setApiToken(oldConfiguration.getApiToken());
    }
    if (DUMMY_SECRET.equals(target.getGitAuthenticationToken())) {
      target.setGitAuthenticationToken(oldConfiguration.getGitAuthenticationToken());
    }
  }

  @AfterMapping
  void appendLinks(@MappingTarget GlobalJenkinsConfigurationDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(NAME).isPermitted()) {
      linksBuilder.single(link("update", update()));
    }
    if (ConfigurationPermissions.read(NAME).isPermitted()) {
      target.add(linksBuilder.build());
    }
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("getGlobalJenkinsConfig").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("updateGlobalJenkinsConfig").parameters().href();
  }
}
