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

import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
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
  @VisibleForTesting
  @EqualsAndHashCode(callSuper = true)
  public static final class JenkinsEventDto extends JenkinsEventRelay.JenkinsEventDto {
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
