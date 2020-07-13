package sonia.scm.jenkins;

import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class JenkinsPullRequestEventRelay extends JenkinsEventRelay {

  @Inject
  public JenkinsPullRequestEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient) {
    super(jenkinsContext, repositoryServiceFactory, httpClient);
  }

  @Subscribe
  public void handle(PullRequestEvent event) {
    if (event.getEventType().isPost()) {
      final Repository repository = event.getRepository();

      try (final RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
        final List<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols().collect(Collectors.toList());

        final JenkinsEventDto eventDto = new JenkinsEventDto(
          event.getEventType(),
          event.getPullRequest().getId(),
          event.getRepository().getName(),
          event.getRepository().getNamespace(),
          supportedProtocols
        );

        this.send(repository, eventDto);
      }
    }
  }

  @Getter
  @Setter
  private static final class JenkinsEventDto extends JenkinsEventRelay.JenkinsEventDto {
    private HandlerEventType eventType;
    private String pullRequestId;
    private String repositoryName;
    private String repositoryNamespace;

    public JenkinsEventDto(HandlerEventType eventType, String pullRequestId, String repositoryName, String repositoryNamespace, List<ScmProtocol> protocols) {
      super(protocols);
      this.eventType = eventType;
      this.pullRequestId = pullRequestId;
      this.repositoryName = repositoryName;
      this.repositoryNamespace = repositoryNamespace;
    }
  }
}
