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
