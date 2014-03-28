/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.jenkins;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.HttpClient;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Collection;

/**
 * Jenkins post receive Hook.
 * This class is called after a changeset successfully pushed to a repository.
 * The jenkins hook checks if the hook is configured for this repository and
 * calls the configured jenkins ci server if a valid configuration is a
 * available. This class is a singleton, so be carefully.
 *
 * Extension objects in SCM-Manager:
 * https://bitbucket.org/sdorra/scm-manager/wiki/ExtensionPoints
 *
 * @author Sebastian Sdorra
 */
@Extension
public class JenkinsHook implements RepositoryHook
{

  /** the logger for JenkinsHook */
  private static final Logger logger =
    LoggerFactory.getLogger(JenkinsHook.class);

  /** The type(s) of the repository hooks. */
  public static final Collection<RepositoryHookType> TYPES =
    Arrays.asList(RepositoryHookType.POST_RECEIVE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Creates a new instance of Jenkins Hook. This constructor is called by
   * Google Guice.Guice injects all necessary dependencies to the constructor.
   * For more informations on Google Guice contructor injection have a look
   * at http://code.google.com/p/google-guice/wiki/Injections.
   *
   * Available objects for injection in SCM-Manager:
   * https://bitbucket.org/sdorra/scm-manager/wiki/injectionObjects
   *
   *
   * @param repositoryManager
   * @param scmConfiguration
   * @param httpClientProvider Google Guice provider for an {@link HttpClient}
   * @param context
   */
  @Inject
  public JenkinsHook(RepositoryManager repositoryManager,
                     ScmConfiguration scmConfiguration,
                     Provider<HttpClient> httpClientProvider,
                     JenkinsContext context)
  {
    this.repositoryManager = repositoryManager;
    this.scmConfiguration = scmConfiguration;
    this.httpClientProvider = httpClientProvider;
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * This method is called after a changeset is successfully pushed to a
   * repository.
   *
   *
   * @param event the event of the hook. This object contains the changed
   *        repository and the changesets.
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {

    // get the changed repository
    Repository repository = event.getRepository();

    /**
     * check if the repository is not null. If the repository is null
     * log an error.
     */
    if (repository != null)
    {
      GlobalJenkinsConfiugration globalConfig = context.getConfiguration();
      JenkinsHookHandler handler = null;

      if (!globalConfig.isDisableRepositoryConfiguration())
      {

        // read jenkins configuration from repository
        JenkinsConfiguration configuration =
          new JenkinsConfiguration(repository);

        // check if the configuration is valid and log error if not
        if (configuration.isValid())
        {
          handler = new JenkinsRepositoryHookHandler(httpClientProvider,
                  configuration);
        }
        else
        {
          logger.debug("jenkins configuration for repository {} is not valid, try global configuration",
                        repository.getName());

          handler = new JenkinsGlobalHookHandler(repositoryManager,
                  scmConfiguration, httpClientProvider, globalConfig,
                  repository);
        }
      }
      else
      {
        handler = new JenkinsGlobalHookHandler(repositoryManager,
                scmConfiguration, httpClientProvider, globalConfig, repository);
      }

      handler.sendRequest();
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("receive repository hook without repository");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the types of the hook.
   *
   * @return a {@link Collection} of repository types.
   */
  @Override
  public Collection<RepositoryHookType> getTypes()
  {
    return TYPES;
  }

  /**
   * Return true if the hook can be executed asynchronous.
   * Note you can not access the current servlet or user if the hook is executed
   * asynchronous.
   *
   * @return true if the hook is executed asynchronous.
   */
  @Override
  public boolean isAsync()
  {
    return true;
  }

  //~--- fields ---------------------------------------------------------------

  /** Global jenkins configuration */
  private JenkinsContext context;

  /** Guice provider for {@link HttpClient} */
  private Provider<HttpClient> httpClientProvider;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private ScmConfiguration scmConfiguration;
}
