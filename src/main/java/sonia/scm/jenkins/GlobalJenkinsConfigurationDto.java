package sonia.scm.jenkins;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalJenkinsConfigurationDto extends HalRepresentation {

  private boolean disableRepositoryConfiguration = false;
  private boolean disableMercurialTrigger = false;
  private boolean disableGitTrigger = false;
  private String url;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
