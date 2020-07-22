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

import com.cloudogu.scm.review.pullrequest.service.BasicPullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestUpdatedEvent;
import com.github.legman.Subscribe;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Extension
@EagerSingleton
@Requires("scm-review-plugin")
public class JenkinsPullRequestEventRelay extends JenkinsEventRelay {

  @Inject
  public JenkinsPullRequestEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient, ScmConfiguration configuration) {
    super(configuration, jenkinsContext, repositoryServiceFactory, httpClient);
  }

  @Subscribe
  public void handle(PullRequestEvent event) {
    if (event.getEventType() == HandlerEventType.CREATE) {
      handle(event, JenkinsPullRequestEventDto::setCreateOrModifiedPullRequests);
    }
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

    try (final RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      final List<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols().collect(Collectors.toList());

      final JenkinsPullRequestEventDto eventDto = new JenkinsPullRequestEventDto(supportedProtocols);
      List<PullRequestDto> pullRequestDtos = Collections.singletonList(new PullRequestDto(event.getPullRequest()));
      sender.accept(eventDto, pullRequestDtos);

      this.send(repository, eventDto);
    }
  }

  @Getter
  @Setter
  public static final class JenkinsPullRequestEventDto extends JenkinsEventRelay.JenkinsEventDto {

    private List<PullRequestDto> deletedPullRequests;
    private List<PullRequestDto> createOrModifiedPullRequests;

    public JenkinsPullRequestEventDto(List<ScmProtocol> protocols) {
      super(protocols);
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
