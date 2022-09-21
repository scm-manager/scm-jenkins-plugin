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

import com.cloudogu.scm.el.ElParser;
import com.github.legman.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.jenkins.hooks.GitGlobalHookHandler;
import sonia.scm.jenkins.hooks.GitRepositoryHookHandler;
import sonia.scm.jenkins.hooks.HgGlobalHookHandler;
import sonia.scm.jenkins.hooks.HgRepositoryHookHandler;
import sonia.scm.jenkins.hooks.SvnGlobalHookHandler;
import sonia.scm.jenkins.hooks.SvnHookHandlerHelper;
import sonia.scm.jenkins.hooks.SvnRepositoryHookHandler;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;

/**
 * Jenkins post receive Hook.
 * This class is called after a changeset successfully pushed to a repository.
 * The jenkins hook checks if the hook is configured for this repository and
 * calls the configured jenkins ci server if a valid configuration is a
 * available. This class is a singleton, so be carefully.
 * <p>
 *
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class JenkinsHook {

  private static final Logger LOG = LoggerFactory.getLogger(JenkinsHook.class);

  private final JenkinsContext context;
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory repositoryServiceFactory;
  private final ElParser elParser;

  @Inject
  public JenkinsHook(Provider<AdvancedHttpClient> httpClientProvider,
                     JenkinsContext context,
                     RepositoryServiceFactory repositoryServiceFactory,
                     ElParser elParser) {
    this.httpClientProvider = httpClientProvider;
    this.context = context;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.elParser = elParser;
  }

  /**
   * This method is called after a changeset is successfully pushed to a
   * repository.
   *
   * @param event the event of the hook. This object contains the changed
   *              repository and the changesets.
   */
  @Subscribe
  public void onEvent(PostReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    if (repository != null) {
      GlobalJenkinsConfiguration globalConfig = context.getConfiguration();
      JenkinsHookHandler handler;

      if (!globalConfig.isDisableRepositoryConfiguration()) {
        JenkinsConfiguration configuration = context.getConfiguration(repository);
        if (configuration.isValid()) {
          handler = getRepositoryHookHandler(repository);
        } else {
          LOG.debug("jenkins configuration for repository {}/{} is not valid, try global configuration", repository.getNamespace(), repository.getName());
          handler = getGlobalHookHandler(repository);
        }
      } else {
        handler = getGlobalHookHandler(repository);
      }

      handler.sendRequest(event);
    } else if (LOG.isWarnEnabled()) {
      LOG.warn("receive repository hook without repository");
    }
  }

  private JenkinsHookHandler getRepositoryHookHandler(Repository repository) {
    switch (repository.getType()) {
      case SvnHookHandlerHelper.TYPE: {
        return new SvnRepositoryHookHandler(httpClientProvider, repositoryServiceFactory, context.getConfiguration(repository));
      }
      case GitGlobalHookHandler.TYPE: {
        return new GitRepositoryHookHandler(httpClientProvider, elParser, context.getConfiguration(repository));
      }
      case HgGlobalHookHandler.TYPE: {
        return new HgRepositoryHookHandler(httpClientProvider, elParser, context.getConfiguration(repository));
      }
      default:
        throw new IllegalStateException("Repository type is not supported");
    }
  }

  private JenkinsHookHandler getGlobalHookHandler(Repository repository) {
    switch (repository.getType()) {
      case SvnHookHandlerHelper.TYPE: {
        return new SvnGlobalHookHandler(httpClientProvider, repositoryServiceFactory, context.getConfiguration());
      }
      case GitGlobalHookHandler.TYPE: {
        return new GitGlobalHookHandler(httpClientProvider, repositoryServiceFactory, context.getConfiguration());
      }
      case HgGlobalHookHandler.TYPE: {
        return new HgGlobalHookHandler(httpClientProvider, repositoryServiceFactory, context.getConfiguration());
      }
      default:
        throw new IllegalStateException("Repository type is not supported");
    }
  }
}
