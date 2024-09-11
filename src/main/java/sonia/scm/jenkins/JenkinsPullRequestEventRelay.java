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

import com.cloudogu.scm.review.pullrequest.service.BasicPullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestReopenedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestUpdatedEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ScmProtocol;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class JenkinsPullRequestEventRelay {

  private final JenkinsEventRelay jenkinsEventRelay;
  private final ProtocolResolver protocolResolver;

  @Inject
  public JenkinsPullRequestEventRelay(JenkinsEventRelay jenkinsEventRelay, ProtocolResolver protocolResolver) {
    this.jenkinsEventRelay = jenkinsEventRelay;
    this.protocolResolver = protocolResolver;
  }

  @Subscribe
  public void handle(PullRequestEvent event) {
    if (event.getEventType() == HandlerEventType.CREATE) {
      handle(event, JenkinsPullRequestEventDto::setCreateOrModifiedPullRequests);
    }
  }

  @Subscribe
  public void handleReopenedEvent(PullRequestReopenedEvent event) {
    handle(event, JenkinsPullRequestEventDto::setCreateOrModifiedPullRequests);
  }

  @Subscribe
  public void handleUpdatedEvent(PullRequestUpdatedEvent event) {
    handle(event, JenkinsPullRequestEventDto::setCreateOrModifiedPullRequests);
  }

  @Subscribe
  public void handleMergedEvent(PullRequestMergedEvent event) {
    handle(event, JenkinsPullRequestEventDto::setDeletedPullRequests);
  }

  @Subscribe
  public void handleRejectedEvent(PullRequestRejectedEvent event) {
    handle(event, JenkinsPullRequestEventDto::setDeletedPullRequests);
  }

  private void handle(BasicPullRequestEvent event, BiConsumer<JenkinsPullRequestEventDto, List<PullRequestDto>> sender) {
    final Repository repository = event.getRepository();

    final List<ScmProtocol> supportedProtocols = protocolResolver.getProtocols(repository);

    final JenkinsPullRequestEventDto eventDto = new JenkinsPullRequestEventDto(supportedProtocols);
    List<PullRequestDto> pullRequestDtos = Collections.singletonList(new PullRequestDto(event.getPullRequest()));
    sender.accept(eventDto, pullRequestDtos);

    jenkinsEventRelay.send(repository, eventDto);
  }

  @Getter
  @Setter
  @SuppressWarnings("java:S2160")
  public static final class JenkinsPullRequestEventDto extends JenkinsRepositoryEventDto {

    private List<PullRequestDto> deletedPullRequests;
    private List<PullRequestDto> createOrModifiedPullRequests;

    public JenkinsPullRequestEventDto(List<ScmProtocol> protocols) {
      super(EventTarget.SOURCE, protocols);
    }
  }

  @Getter
  public static class PullRequestDto {
    private String id;
    private String source;
    private String target;

    public PullRequestDto(PullRequest pullRequest) {
      this.id = pullRequest.getId();
      this.source = pullRequest.getSource();
      this.target = pullRequest.getTarget();
    }
  }
}
