package sonia.scm.jenkins;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.store.ConfigurationStore;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsEventRelay {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsEventRelay.class);

  protected final JenkinsContext jenkinsContext;
  protected final RepositoryServiceFactory repositoryServiceFactory;
  protected final AdvancedHttpClient httpClient;

  public JenkinsEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient) {
    this.jenkinsContext = jenkinsContext;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.httpClient = httpClient;
  }

  protected final void send(Repository repository, JenkinsEventDto eventDto) {
    String url;
    if (jenkinsContext.getConfiguration().isDisableRepositoryConfiguration()) {
      url = jenkinsContext.getConfiguration().getUrl();
    } else {
      url = jenkinsContext.getConfiguration(repository).getUrl();
    }

    try {
      httpClient.post(url).jsonContent(eventDto).request();
    } catch (IOException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Failed to relay event to Jenkins server");
      }
    }
  }

  @Getter
  @Setter
  public static class JenkinsEventDto extends HalRepresentation {
    JenkinsEventDto(List<ScmProtocol> protocols) {
      super(new Links.Builder().array(protocols.stream().map(protocol -> Link.link(protocol.getType(), protocol.getUrl())).collect(Collectors.toList())).build());
    }
  }
}
