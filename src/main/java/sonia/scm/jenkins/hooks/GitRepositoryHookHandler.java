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

package sonia.scm.jenkins.hooks;

import com.cloudogu.scm.el.ElParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.jenkins.JenkinsHookHandler;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.RepositoryHookEvent;

import javax.inject.Provider;

public class GitRepositoryHookHandler implements JenkinsHookHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GitRepositoryHookHandler.class);

  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final ElParser elParser;
  private final JenkinsConfiguration configuration;

  public GitRepositoryHookHandler(Provider<AdvancedHttpClient> httpClientProvider, ElParser elParser, JenkinsConfiguration configuration) {
    this.httpClientProvider = httpClientProvider;
    this.elParser = elParser;
    this.configuration = configuration;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    String url = RepositoryHookUtils.createUrl(configuration);
    if (configuration.getBranches().isEmpty()) {
      LOG.debug("branch list is empty, send request");

      RepositoryHookUtils.handleRepositoryEvent(httpClientProvider, elParser, configuration, url,  event);
    } else {
      if (RepositoryHookUtils.isInBranchSet(configuration.getBranches(), event.getContext().getChangesetProvider().getChangesets())) {
        RepositoryHookUtils.handleRepositoryEvent(httpClientProvider, elParser, configuration, url, event);
      } else {
        LOG.debug("changesets does not contain configured branches");
      }
    }
  }
}
