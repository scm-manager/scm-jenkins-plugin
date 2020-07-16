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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JenkinsEventRelay {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsEventRelay.class);

  protected final JenkinsContext jenkinsContext;
  protected final RepositoryServiceFactory repositoryServiceFactory;
  protected final AdvancedHttpClient httpClient;

  public JenkinsEventRelay(JenkinsContext jenkinsContext, RepositoryServiceFactory repositoryServiceFactory, AdvancedHttpClient httpClient) {
    this.jenkinsContext = jenkinsContext;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.httpClient = httpClient;
  }

  protected final void send(Repository repository, JenkinsEventDto eventDto) {
    String url;
    if (!jenkinsContext.getConfiguration().isDisableEventTrigger()) {
      if (jenkinsContext.getConfiguration().isDisableRepositoryConfiguration()) {
        url = jenkinsContext.getConfiguration().getUrl();
      } else {
        url = jenkinsContext.getConfiguration(repository).getUrl();
      }

      try {
        httpClient.post(url).jsonContent(eventDto).request();
      } catch (IOException e) {
        if (logger.isWarnEnabled()) {
          logger.warn("Failed to relay event to Jenkins server");
        }
      }
    }
  }

  @Getter
  @Setter
  protected static class JenkinsEventDto extends HalRepresentation {
    JenkinsEventDto(List<ScmProtocol> protocols) {
      super(new Links.Builder().array(protocols.stream().map(protocol -> Link.link(protocol.getType(), protocol.getUrl())).collect(Collectors.toList())).build());
    }
  }
}
