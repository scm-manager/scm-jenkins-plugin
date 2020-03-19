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

import javax.inject.Provider;

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
        .method("getForRepository")
        .parameters(namespace, name)
        .href();
      this.addLink(repositoryNode, "jenkinsConfig", linkBuilder);
    }
  }
}
