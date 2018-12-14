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

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class GlobalJenkinsConfigurationMapper {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public abstract GlobalJenkinsConfigurationDto map(GlobalJenkinsConfiguration config, @Context ScmConfiguration scmConfiguration);

  public abstract GlobalJenkinsConfiguration map(GlobalJenkinsConfigurationDto dto);

  @AfterMapping
  void appendLinks(@MappingTarget GlobalJenkinsConfigurationDto target, @Context ScmConfiguration scmConfiguration) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(scmConfiguration).isPermitted()) {
      linksBuilder.single(link("update", update()));
    }
    if (ConfigurationPermissions.read(scmConfiguration).isPermitted()) {
      target.add(linksBuilder.build());
    }
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}
