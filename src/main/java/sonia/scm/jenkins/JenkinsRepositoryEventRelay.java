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
import lombok.Getter;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.api.ScmProtocol;

import javax.inject.Inject;
import java.util.List;

@Extension
@EagerSingleton
public class JenkinsRepositoryEventRelay {

  private final JenkinsEventRelay jenkinsEventRelay;
  private final ProtocolResolver protocolResolver;

  @Inject
  public JenkinsRepositoryEventRelay(JenkinsEventRelay jenkinsEventRelay, ProtocolResolver protocolResolver) {
    this.jenkinsEventRelay = jenkinsEventRelay;
    this.protocolResolver = protocolResolver;
  }

  @Subscribe
  public void handleRepositoryEvent(RepositoryEvent event) {
    switch (event.getEventType()) {
      case DELETE:
        handleDeletedRepository(event);
        break;
      case CREATE:
        handleCreatedRepository(event);
        break;
      case MODIFY:
        handleModifiedRepository(event);
        break;
      default:
        // nothing to do for this event
    }
  }

  private void handleDeletedRepository(RepositoryEvent event) {
    sendEvent(event.getOldItem(), HandlerEventType.DELETE);
  }

  private void handleCreatedRepository(RepositoryEvent event) {
    sendEvent(event.getItem(), HandlerEventType.CREATE);
  }

  private void handleModifiedRepository(RepositoryEvent event) {
    sendEvent(event.getItem(), HandlerEventType.MODIFY);
  }

  private void sendEvent(Repository repository, HandlerEventType create) {
    List<ScmProtocol> supportedProtocols = protocolResolver.getProtocols(repository);
    RepositoryJenkinsEventDto eventDto = new RepositoryJenkinsEventDto(create, supportedProtocols);
    jenkinsEventRelay.send(repository, eventDto);
  }

  @Getter
  static class RepositoryJenkinsEventDto extends JenkinsEventDto {

    private final HandlerEventType changeType;

    RepositoryJenkinsEventDto(HandlerEventType changeType, List<ScmProtocol> protocols) {
      super(EventTarget.NAVIGATOR, protocols);
      this.changeType = changeType;
    }
  }
}
