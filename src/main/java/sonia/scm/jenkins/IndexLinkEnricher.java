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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.jenkins.JenkinsContext.NAME;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class IndexLinkEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public IndexLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read(NAME).isPermitted()) {
      String globalJenkinsConfigUrl = new LinkBuilder(scmPathInfoStore.get().get(), JenkinsConfigurationResource.class)
        .method("getGlobalJenkinsConfig")
        .parameters()
        .href();

      JsonNode hgConfigRefNode = createObject(singletonMap("href", value(globalJenkinsConfigUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "jenkinsConfig", hgConfigRefNode);
    }
  }
}
