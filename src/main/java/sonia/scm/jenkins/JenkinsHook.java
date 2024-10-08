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

import com.cloudogu.scm.el.ElParser;
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

import static sonia.scm.jenkins.JenkinsGitGlobalHookHandler.TYPE_GIT;
import static sonia.scm.jenkins.JenkinsHgGlobalHookHandler.TYPE_MERCURIAL;
import static sonia.scm.jenkins.JenkinsSvnGlobalHookHandler.TYPE_SUBVERSION;

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
  private final ElParser elParser;

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
   * @param elParser
   */
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

    // get the changed repository
    Repository repository = event.getRepository();

    /*
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
          handler = new JenkinsRepositoryHookHandler(httpClientProvider, configuration, elParser);
        } else {
          logger.debug("jenkins configuration for repository {}/{} is not valid, try global configuration",
            repository.getNamespace(), repository.getName());

          handler = getGlobalHookHandler(repository, globalConfig);
        }
      } else {
        handler = getGlobalHookHandler(repository, globalConfig);
      }

      handler.sendRequest(event);
    } else if (logger.isWarnEnabled()) {
      logger.warn("receive repository hook without repository");
    }
  }

  private JenkinsHookHandler getGlobalHookHandler(Repository repository, GlobalJenkinsConfiguration globalConfig) {
    switch (repository.getType()) {
      case TYPE_SUBVERSION:
        return new JenkinsSvnGlobalHookHandler(httpClientProvider, globalConfig, repositoryServiceFactory);
      case TYPE_GIT:
        return new JenkinsGitGlobalHookHandler(httpClientProvider, globalConfig, repositoryServiceFactory);
      case TYPE_MERCURIAL:
        return new JenkinsHgGlobalHookHandler(httpClientProvider, globalConfig, repositoryServiceFactory);
      default:
        throw new IllegalStateException("unknown repository type: " + repository.getType());
    }
  }
}
