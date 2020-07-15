/*
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

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import javax.inject.Inject;
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
      final HookContext context = event.getContext();

      if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER) || context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
        final List<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols().collect(Collectors.toList());
        final JenkinsEventDto eventDto = new JenkinsEventDto(supportedProtocols);

        if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
          eventDto.setCreatedOrModifiedBranches(context.getBranchProvider().getCreatedOrModified());
          eventDto.setDeletedBranches(context.getBranchProvider().getDeletedOrClosed());
        }

        if (context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
          eventDto.setCreateOrModifiedTags(context.getTagProvider().getCreatedTags());
          eventDto.setDeletedTags(context.getTagProvider().getDeletedTags());
        }

        this.send(repository, eventDto);
      }
    }
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = true)
  @VisibleForTesting
  public static final class JenkinsEventDto extends JenkinsEventRelay.JenkinsEventDto {
    private List<String> deletedBranches;
    private List<String> createdOrModifiedBranches;
    private List<Tag> deletedTags;
    private List<Tag> createOrModifiedTags;

    public JenkinsEventDto(List<ScmProtocol> protocols) {
      super(protocols);
    }
  }
}
