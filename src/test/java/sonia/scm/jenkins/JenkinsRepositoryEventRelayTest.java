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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.security.AuthorizationChangedEvent;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JenkinsRepositoryEventRelayTest {

  @Mock
  private JenkinsEventRelay jenkinsEventRelay;

  @InjectMocks
  private JenkinsRepositoryEventRelay relay;

  @Test
  void shouldSendEventForAuthorizationEvent() {
    AuthorizationChangedEvent event = AuthorizationChangedEvent.createForEveryUser();

    relay.handleAuthorizationEvent(event);

    verify(jenkinsEventRelay).send(argThat(Objects::nonNull));
  }

  @Test
  void shouldSendEventForRepositoryModificationWhenNameHasBeenModifiedEvent() {
    Repository oldVersion = RepositoryTestData.createHeartOfGold();
    Repository newVersion = RepositoryTestData.createHeartOfGold();
    newVersion.setName("hog2");
    RepositoryModificationEvent event = new RepositoryModificationEvent(HandlerEventType.MODIFY, newVersion, oldVersion);

    relay.handleRepositoryModificationEvent(event);

    verify(jenkinsEventRelay).send(argThat(argument -> {
      Assertions.assertThat(argument.getEventTarget()).isEqualTo(EventTarget.NAVIGATOR);
      return true;
    }));
  }

  @Test
  void shouldSendEventForRepositoryModificationEvent() {
    RepositoryModificationEvent event = new RepositoryModificationEvent(HandlerEventType.MODIFY, RepositoryTestData.createHeartOfGold(), RepositoryTestData.createHeartOfGold());

    relay.handleRepositoryModificationEvent(event);

    verify(jenkinsEventRelay, never()).send(any());
  }
}
