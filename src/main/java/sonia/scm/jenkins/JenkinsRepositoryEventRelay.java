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
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.security.AuthorizationChangedEvent;

import jakarta.inject.Inject;

@Extension
@EagerSingleton
public class JenkinsRepositoryEventRelay {

  private final JenkinsEventRelay jenkinsEventRelay;

  @Inject
  public JenkinsRepositoryEventRelay(JenkinsEventRelay jenkinsEventRelay) {
    this.jenkinsEventRelay = jenkinsEventRelay;
  }

  @Subscribe
  public void handleAuthorizationEvent(AuthorizationChangedEvent event) {
    jenkinsEventRelay.send(new JenkinsEventDto(EventTarget.NAVIGATOR));
  }

  @Subscribe
  public void handleRepositoryModificationEvent(RepositoryModificationEvent event) {
    if (!event.getItem().getName().equals(event.getItemBeforeModification().getName())) {
      jenkinsEventRelay.send(new JenkinsEventDto(EventTarget.NAVIGATOR));
    }
  }
}
