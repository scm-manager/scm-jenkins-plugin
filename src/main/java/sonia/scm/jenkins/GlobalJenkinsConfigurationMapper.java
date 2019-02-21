package sonia.scm.jenkins;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
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

  public abstract GlobalJenkinsConfigurationDto map(GlobalJenkinsConfiguration config);

  public abstract GlobalJenkinsConfiguration map(GlobalJenkinsConfigurationDto dto);

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
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), JenkinsConfigurationResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}
