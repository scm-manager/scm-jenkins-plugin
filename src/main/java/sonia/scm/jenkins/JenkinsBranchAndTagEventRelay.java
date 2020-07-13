package sonia.scm.jenkins;

import com.github.legman.Subscribe;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.store.ConfigurationStore;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Extension
@EagerSingleton
public class JenkinsBranchAndTagEventRelay extends JenkinsEventRelay {

  @Inject
  public JenkinsBranchAndTagEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient) {
    super(jenkinsContext, repositoryServiceFactory, httpClient);
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    final Repository repository = event.getRepository();

    try (final RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      final List<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols().collect(Collectors.toList());

      final JenkinsEventDto eventDto = new JenkinsEventDto(supportedProtocols);

      final HookContext context = event.getContext();

      if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
        final HookBranchProvider branchProvider = context.getBranchProvider();
        eventDto.createdOrModifiedBranches = branchProvider.getCreatedOrModified();
        eventDto.deletedBranches = branchProvider.getDeletedOrClosed();
      }

      if (context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
        final HookTagProvider tagProvider = context.getTagProvider();

        eventDto.createOrModifiedTags = tagProvider.getCreatedTags();
        eventDto.deletedTags = tagProvider.getDeletedTags();
      }

      this.send(repository, eventDto);
    }

  }

  @Getter
  @Setter
  private static final class JenkinsEventDto extends JenkinsEventRelay.JenkinsEventDto {
    private List<String> deletedBranches;
    private List<String> createdOrModifiedBranches;
    private List<Tag> deletedTags;
    private List<Tag> createOrModifiedTags;

    public JenkinsEventDto(List<ScmProtocol> protocols) {
      super(protocols);
    }
  }
}
