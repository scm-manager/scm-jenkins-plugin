/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.jenkins;

import com.github.legman.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.ScmProtocol;

import jakarta.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Extension
@EagerSingleton
public class JenkinsBranchAndTagEventRelay {

  private final JenkinsEventRelay jenkinsEventRelay;
  private final ProtocolResolver protocolResolver;

  @Inject
  public JenkinsBranchAndTagEventRelay(JenkinsEventRelay jenkinsEventRelay, ProtocolResolver protocolResolver) {
    this.jenkinsEventRelay = jenkinsEventRelay;
    this.protocolResolver = protocolResolver;
  }

  @Subscribe
  public void handle(PostReceiveRepositoryHookEvent event) {
    final Repository repository = event.getRepository();

      final HookContext context = event.getContext();

      if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER) || context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
        final List<ScmProtocol> supportedProtocols = protocolResolver.getProtocols(repository);
        final JenkinsBranchAndTagEventDto eventDto = new JenkinsBranchAndTagEventDto(supportedProtocols);

        if (context.isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
          eventDto.setCreatedOrModifiedBranches(context.getBranchProvider().getCreatedOrModified().stream().map(BranchDto::new).collect(toList()));
          eventDto.setDeletedBranches(context.getBranchProvider().getDeletedOrClosed().stream().map(BranchDto::new).collect(toList()));
        }

        if (context.isFeatureSupported(HookFeature.TAG_PROVIDER)) {
          eventDto.setCreateOrModifiedTags(context.getTagProvider().getCreatedTags().stream().map(TagDto::new).collect(toList()));
          eventDto.setDeletedTags(context.getTagProvider().getDeletedTags().stream().map(TagDto::new).collect(toList()));
        }

        jenkinsEventRelay.send(repository, eventDto);
      }
  }

  @Getter
  @Setter
  @SuppressWarnings("java:S2160")
  public static final class JenkinsBranchAndTagEventDto extends JenkinsRepositoryEventDto {
    private List<BranchDto> deletedBranches;
    private List<BranchDto> createdOrModifiedBranches;
    private List<TagDto> deletedTags;
    private List<TagDto> createOrModifiedTags;

    private String namespace;
    private String name;
    private String type;
    private String server;

    public JenkinsBranchAndTagEventDto(List<ScmProtocol> protocols) {
      super(EventTarget.SOURCE, protocols);
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
