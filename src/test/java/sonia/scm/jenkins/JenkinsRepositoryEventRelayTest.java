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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.jenkins.JenkinsRepositoryEventRelay.RepositoryJenkinsEventDto;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JenkinsRepositoryEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private JenkinsEventRelay jenkinsEventRelay;
  @Mock
  private ProtocolResolver protocolResolver;

  @InjectMocks
  private JenkinsRepositoryEventRelay relay;

  @ParameterizedTest()
  @EnumSource(names = {"BEFORE_CREATE", "BEFORE_MODIFY", "BEFORE_DELETE"})
  void shouldIngnoreIrrelevantEvents(HandlerEventType eventType) {
    RepositoryEvent event = new RepositoryEvent(eventType, null, null);

    relay.handleRepositoryEvent(event);

    verify(jenkinsEventRelay, never()).send(any(), any());
  }

  @Test
  void shouldSendEventForDeletedRepository() {
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, null, REPOSITORY);

    relay.handleRepositoryEvent(event);

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      RepositoryJenkinsEventDto repositoryJenkinsEventDto = (RepositoryJenkinsEventDto) dto;
      Assertions.assertThat(repositoryJenkinsEventDto.getChangeType()).isEqualTo(HandlerEventType.DELETE);
      return true;
    }));
  }

  @Test
  void shouldSendEventForCreatedRepository() {
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.CREATE, REPOSITORY, null);

    relay.handleRepositoryEvent(event);

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      RepositoryJenkinsEventDto repositoryJenkinsEventDto = (RepositoryJenkinsEventDto) dto;
      Assertions.assertThat(repositoryJenkinsEventDto.getChangeType()).isEqualTo(HandlerEventType.CREATE);
      return true;
    }));
  }

  @Test
  void shouldSendEventForModifiedRepository() {
    RepositoryEvent event = new RepositoryEvent(HandlerEventType.MODIFY, REPOSITORY, RepositoryTestData.createHeartOfGold());

    relay.handleRepositoryEvent(event);

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      RepositoryJenkinsEventDto repositoryJenkinsEventDto = (RepositoryJenkinsEventDto) dto;
      Assertions.assertThat(repositoryJenkinsEventDto.getChangeType()).isEqualTo(HandlerEventType.MODIFY);
      return true;
    }));
  }
}
