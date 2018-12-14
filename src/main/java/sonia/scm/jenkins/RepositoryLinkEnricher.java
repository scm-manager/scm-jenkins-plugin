package sonia.scm.jenkins;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.web.AbstractRepositoryJsonEnricher;

import javax.inject.Provider;

@Extension
public class RepositoryLinkEnricher extends AbstractRepositoryJsonEnricher {

  private final JenkinsContext jenkinsContext;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public RepositoryLinkEnricher(ObjectMapper objectMapper, JenkinsContext jenkinsContext, Provider<ScmPathInfoStore> scmPathInfoStore) {
    super(objectMapper);
    this.jenkinsContext = jenkinsContext;
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
    if (!jenkinsContext.getConfiguration().isDisableRepositoryConfiguration()) {
      String linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), JenkinsConfigurationResource.class)
        .method("getForRepository")
        .parameters(namespace, name)
        .href();
      this.addLink(repositoryNode, "jenkinsConfig", linkBuilder);
    }
  }
}
