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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.config.ScmConfiguration;
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

import static java.util.stream.Collectors.toList;

@Extension
@EagerSingleton
public class JenkinsBranchAndTagEventRelay extends JenkinsEventRelay {

  @Inject
  public JenkinsBranchAndTagEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient, ScmConfiguration configuration) {
    super(configuration, jenkinsContext, repositoryServiceFactory, httpClient);
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    final Repository repository = event.getRepository();

    try (final RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      final HookContext context = event.getContext();

      if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER) || context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
        final List<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols().collect(toList());
        final JenkinsBranchAndTagEventDto eventDto = new JenkinsBranchAndTagEventDto(supportedProtocols);

        if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
          eventDto.setCreatedOrModifiedBranches(context.getBranchProvider().getCreatedOrModified().stream().map(BranchDto::new).collect(toList()));
          eventDto.setDeletedBranches(context.getBranchProvider().getDeletedOrClosed().stream().map(BranchDto::new).collect(toList()));
        }

        if (context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
          eventDto.setCreateOrModifiedTags(context.getTagProvider().getCreatedTags().stream().map(TagDto::new).collect(toList()));
          eventDto.setDeletedTags(context.getTagProvider().getDeletedTags().stream().map(TagDto::new).collect(toList()));
        }

        this.send(repository, eventDto);
      }
    }
  }

  @Getter
  @Setter
  public static final class JenkinsBranchAndTagEventDto extends JenkinsEventRelay.JenkinsEventDto {
    private List<BranchDto> deletedBranches;
    private List<BranchDto> createdOrModifiedBranches;
    private List<TagDto> deletedTags;
    private List<TagDto> createOrModifiedTags;

    private String namespace;
    private String name;
    private String type;
    private String server;

    public JenkinsBranchAndTagEventDto(List<ScmProtocol> protocols) {
      super(protocols);
    }
  }

  @Getter
  @AllArgsConstructor
  public static class BranchDto {
    private String name;
  }

  @Getter
  public static class TagDto {
    private String name;

    TagDto(Tag tag) {
      this.name = tag.getName();
    }
  }
}
