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
import com.google.inject.Inject;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.AbstractRepositoryJsonEnricher;

import jakarta.inject.Provider;

import static sonia.scm.jenkins.JenkinsContext.NAME;

@Extension
public class RepositoryLinkEnricher extends AbstractRepositoryJsonEnricher {

  private final JenkinsContext jenkinsContext;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final RepositoryManager repositoryManager;

  @Inject
  public RepositoryLinkEnricher(ObjectMapper objectMapper, JenkinsContext jenkinsContext, Provider<ScmPathInfoStore> scmPathInfoStore, RepositoryManager repositoryManager) {
    super(objectMapper);
    this.jenkinsContext = jenkinsContext;
    this.scmPathInfoStore = scmPathInfoStore;
    this.repositoryManager = repositoryManager;
  }

  @Override
  protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (!jenkinsContext.getConfiguration().isDisableRepositoryConfiguration()
      && RepositoryPermissions.custom(NAME, repository).isPermitted()) {
      String linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), JenkinsConfigurationResource.class)
        .method("getJenkinsConfigForRepository")
        .parameters(namespace, name)
        .href();
      this.addLink(repositoryNode, "jenkinsConfig", linkBuilder);
    }
  }
}
