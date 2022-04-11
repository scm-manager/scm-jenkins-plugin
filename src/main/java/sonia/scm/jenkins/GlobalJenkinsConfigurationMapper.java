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

import javax.inject.Inject;

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
  }

  @AfterMapping
  public void restoreSecretsOnDummy(@MappingTarget GlobalJenkinsConfiguration target, @Context GlobalJenkinsConfiguration oldConfiguration) {
    if (DUMMY_SECRET.equals(target.getApiToken())) {
      target.setApiToken(oldConfiguration.getApiToken());
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
