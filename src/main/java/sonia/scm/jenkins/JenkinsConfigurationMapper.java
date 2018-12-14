package sonia.scm.jenkins;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class JenkinsConfigurationMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract JenkinsConfigurationDto map(JenkinsConfiguration config, @Context Repository repository);

  public abstract JenkinsConfiguration map(JenkinsConfigurationDto dto);

  @AfterMapping
  void appendLinks(@MappingTarget JenkinsConfigurationDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self(repository));
    if (RepositoryPermissions.modify(repository).isPermitted()) {
      linksBuilder.single(link("update", update(repository)));
    }
    if (RepositoryPermissions.read(repository).isPermitted()) {
      target.add(linksBuilder.build());
    }
  }

  private String self(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("getForRepository").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String update(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("updateForRepository").parameters(repository.getNamespace(), repository.getName()).href();
  }
}