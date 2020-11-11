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

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.security.AuthorizationChangedEvent;

import javax.inject.Inject;

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
