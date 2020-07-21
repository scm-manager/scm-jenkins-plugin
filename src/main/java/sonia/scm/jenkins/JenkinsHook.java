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
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.JexlUrlParser;
/**
 * Jenkins post receive Hook.
 * This class is called after a changeset successfully pushed to a repository.
 * The jenkins hook checks if the hook is configured for this repository and
 * calls the configured jenkins ci server if a valid configuration is a
 * available. This class is a singleton, so be carefully.
 * <p>
 * Extension objects in SCM-Manager:
 * https://bitbucket.org/sdorra/scm-manager/wiki/ExtensionPoints
 *
 * @author Sebastian Sdorra
 */
@Extension
@EagerSingleton
public class JenkinsHook {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsHook.class);

  private final JenkinsContext context;
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory repositoryServiceFactory;
  private final JexlUrlParser urlParser;

  /**
   * Creates a new instance of Jenkins Hook. This constructor is called by
   * Google Guice.Guice injects all necessary dependencies to the constructor.
   * For more informations on Google Guice contructor injection have a look
   * at http://code.google.com/p/google-guice/wiki/Injections.
   * <p>
   * Available objects for injection in SCM-Manager:
   * https://bitbucket.org/sdorra/scm-manager/wiki/injectionObjects
   *
   * @param httpClientProvider       Google Guice provider for an {@link AdvancedHttpClient}
   * @param context
   * @param repositoryServiceFactory
   * @param urlParser
   */
  @Inject
  public JenkinsHook(Provider<AdvancedHttpClient> httpClientProvider,
                     JenkinsContext context,
                     RepositoryServiceFactory repositoryServiceFactory,
                     JexlUrlParser urlParser) {
    this.httpClientProvider = httpClientProvider;
    this.context = context;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.urlParser = urlParser;
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

    // get the changed repository
    Repository repository = event.getRepository();

    /**
     * check if the repository is not null. If the repository is null
     * log an error.
     */
    if (repository != null) {
      GlobalJenkinsConfiguration globalConfig = context.getConfiguration();
      JenkinsHookHandler handler;

      if (!globalConfig.isDisableRepositoryConfiguration()) {

        // read jenkins configuration from repository
        JenkinsConfiguration configuration = context.getConfiguration(repository);

        // check if the configuration is valid and log error if not
        if (configuration.isValid()) {
          handler = new JenkinsRepositoryHookHandler(httpClientProvider,
            configuration, urlParser);
        } else {
          logger.debug("jenkins configuration for repository {} is not valid, try global configuration",
            repository.getName());

          handler = new JenkinsGlobalHookHandler(httpClientProvider, globalConfig, repository, repositoryServiceFactory);
        }
      } else {
        handler = new JenkinsGlobalHookHandler(httpClientProvider, globalConfig, repository, repositoryServiceFactory);
      }

      handler.sendRequest(event);
    } else if (logger.isWarnEnabled()) {
      logger.warn("receive repository hook without repository");
    }
  }
}
