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

import com.cloudogu.scm.review.pullrequest.service.PullRequest;
import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestMergedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestReopenedEvent;
import com.cloudogu.scm.review.pullrequest.service.PullRequestUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static com.cloudogu.scm.review.pullrequest.service.PullRequestRejectedEvent.RejectionCause.REJECTED_BY_USER;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsPullRequestEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final PullRequest PULL_REQUEST = new PullRequest("42", "feature", "master");

  private static final String SERVER_URL = "http://scm-manager.org";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock(answer = Answers.RETURNS_SELF)
  private JenkinsEventRelay jenkinsEventRelay;
  @Mock
  private ProtocolResolver protocolResolver;

  @InjectMocks
  private JenkinsPullRequestEventRelay eventRelay;

  @ParameterizedTest
  @ValueSource(strings = {"BEFORE_CREATE", "BEFORE_MODIFY", "BEFORE_DELETE", "MODIFY"})
  void shouldNotSendForIrrelevantEvents(String eventName) {
    PullRequest pr = new PullRequest();

    eventRelay.handle(new PullRequestEvent(REPOSITORY, pr, pr, HandlerEventType.valueOf(eventName)));

    verify(jenkinsEventRelay, never()).send(any(), any());
  }

  @Nested
  class WithExpectedRequest {

    @Test
    void shouldSendForGlobalConfig() {
      when(protocolResolver.getProtocols(REPOSITORY)).thenReturn(singletonList(new ProtocolResolverTest.DummyScmProtocol()));

      eventRelay.handle(new PullRequestEvent(REPOSITORY, PULL_REQUEST, null, HandlerEventType.CREATE));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto.getLinks()).isNotNull();
        assertThat(dto.getLinks().getLinkBy("dummy")).isNotEmpty();
        return true;
      }));
    }

    @Test
    void shouldSendForCreateEvent() {
      eventRelay.handle(new PullRequestEvent(REPOSITORY, PULL_REQUEST, null, HandlerEventType.CREATE));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("id").containsExactly("42");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("source").containsExactly("feature");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("target").containsExactly("master");
        return true;
      }));
    }

    @Test
    void shouldSendForUpdatedEvent() {
      eventRelay.handleUpdatedEvent(new PullRequestUpdatedEvent(REPOSITORY, PULL_REQUEST));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("id").containsExactly("42");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("source").containsExactly("feature");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("target").containsExactly("master");
        return true;
      }));
    }

    @Test
    void shouldSendForMergedEvent() {
      eventRelay.handleMergedEvent(new PullRequestMergedEvent(REPOSITORY, PULL_REQUEST));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("id").containsExactly("42");
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("source").containsExactly("feature");
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("target").containsExactly("master");
        return true;
      }));
    }

    @Test
    void shouldSendForRejectedEvent() {
      eventRelay.handleRejectedEvent(new PullRequestRejectedEvent(REPOSITORY, PULL_REQUEST, REJECTED_BY_USER));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("id").containsExactly("42");
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("source").containsExactly("feature");
        assertThat(dto).extracting("deletedPullRequests").asList().extracting("target").containsExactly("master");
        return true;
      }));
    }

    @Test
    void shouldSendForReopenedEvent() {
      eventRelay.handleReopenedEvent(new PullRequestReopenedEvent(REPOSITORY, PULL_REQUEST));

      verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("id").containsExactly("42");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("source").containsExactly("feature");
        assertThat(dto).extracting("createOrModifiedPullRequests").asList().extracting("target").containsExactly("master");
        return true;
      }));
    }
  }
}
