package sonia.scm.jenkins;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class JenkinsConfigurationDto extends HalRepresentation {
  private String apiToken;
  private Set<String> branches;
  private String project;
  private String token;
  private String url;
  private String username;
  private boolean csrf;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
