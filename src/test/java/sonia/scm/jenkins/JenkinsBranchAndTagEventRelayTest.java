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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookFeature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JenkinsBranchAndTagEventRelayTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HookContext hookContext;
  @Mock
  private JenkinsEventRelay jenkinsEventRelay;
  @Mock
  private ProtocolResolver protocolResolver;

  @InjectMocks
  private JenkinsBranchAndTagEventRelay eventRelay;

  @Test
  void shouldNotSend() {
    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(jenkinsEventRelay, never()).send(any(), any());
  }

  @Test
  void shouldSendIfBranchesAvailable() {
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(ImmutableList.of("master"));
    when(hookContext.getBranchProvider().getDeletedOrClosed()).thenReturn(ImmutableList.of("develop"));

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      assertThat(dto).extracting("createdOrModifiedBranches").asList().extracting("name").containsExactly("master");
      assertThat(dto).extracting("deletedBranches").asList().extracting("name").containsExactly("develop");
      return true;
    }));
  }

  @Test
  void shouldSendIfTagsAvailable() {
    when(hookContext.isFeatureSupported(HookFeature.TAG_PROVIDER)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(false);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(ImmutableList.of(new Tag("snapshot", "1")));
    when(hookContext.getTagProvider().getDeletedTags()).thenReturn(ImmutableList.of(new Tag("release", "2")));

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      assertThat(dto).extracting("createOrModifiedTags").asList().extracting("name").containsExactly("snapshot");
      assertThat(dto).extracting("deletedTags").asList().extracting("name").containsExactly("release");
      return true;
    }));
  }

  @Test
  void shouldSendIfBranchesAndTagsAreAvailable() {
    when(hookContext.isFeatureSupported(HookFeature.TAG_PROVIDER)).thenReturn(true);
    when(hookContext.isFeatureSupported(HookFeature.BRANCH_PROVIDER)).thenReturn(true);
    when(hookContext.getTagProvider().getCreatedTags()).thenReturn(ImmutableList.of(new Tag("snapshot", "1")));
    when(hookContext.getTagProvider().getDeletedTags()).thenReturn(ImmutableList.of(new Tag("release", "2")));
    when(hookContext.getBranchProvider().getCreatedOrModified()).thenReturn(ImmutableList.of("master"));
    when(hookContext.getBranchProvider().getDeletedOrClosed()).thenReturn(ImmutableList.of("develop"));

    eventRelay.handle(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(hookContext, REPOSITORY, RepositoryHookType.POST_RECEIVE)));

    verify(jenkinsEventRelay).send(eq(REPOSITORY), argThat(dto -> {
      assertThat(dto).extracting("createOrModifiedTags").asList().extracting("name").containsExactly("snapshot");
      assertThat(dto).extracting("deletedTags").asList().extracting("name").containsExactly("release");
      assertThat(dto).extracting("createdOrModifiedBranches").asList().extracting("name").containsExactly("master");
      assertThat(dto).extracting("deletedBranches").asList().extracting("name").containsExactly("develop");
      return true;
    }));
  }
}
