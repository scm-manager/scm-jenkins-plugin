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

import com.google.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static sonia.scm.jenkins.JenkinsContext.NAME;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final JenkinsContext jenkinsContext;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public RepositoryLinkEnricher(JenkinsContext jenkinsContext, Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.jenkinsContext = jenkinsContext;
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext halEnricherContext, HalAppender halAppender) {
    if (jenkinsContext.getConfiguration().isDisableRepositoryConfiguration()) {
      return;
    }
    Repository repository = halEnricherContext.oneRequireByType(Repository.class);
    if (RepositoryPermissions.custom(NAME, repository).isPermitted()) {
      String linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), JenkinsConfigurationResource.class)
        .method("getJenkinsConfigForRepository")
        .parameters(repository.getNamespace(), repository.getName())
        .href();
      halAppender.appendLink("jenkinsConfig", linkBuilder);
    }
  }
}
