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

import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import sonia.scm.repository.RepositoryHookEvent;

/**
 *
 * @author Sebastian Sdorra
 */
public class JenkinsGlobalHookHandler implements JenkinsHookHandler
{
  /** Field description */
  public static final String TYPE_GIT = "git";

  /** Field description */
  public static final String TYPE_MERCURIAL = "hg";

  /** Field description */
  public static final String URL_GIT = "/git/notifyCommit";

  /** Field description */
  public static final String URL_MERCURIAL = "/mercurial/notifyCommit";

  /** the logger for JenkinsGlobalHookHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(JenkinsGlobalHookHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param httpClientProvider
   * @param configuration
   * @param repository
   * @param repositoryServiceFactory
   */
  public JenkinsGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                                  GlobalJenkinsConfiguration configuration, Repository repository, RepositoryServiceFactory repositoryServiceFactory)
  {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
    this.repository = repository;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Fix duplicated slashes in repository url.
   *
   *
   * @see <a target="_blank" href="https://bitbucket.org/sdorra/scm-manager/issue/510/wrong-git-notification-url-to-many-slashes">Issue 510</a>
   * @param url url to fix
   *
   * @return fixed url
   */
  static String fixUrl(String url)
  {
    int index = url.indexOf("://");

    // absolute url
    if (index > 0)
    {
      String suffix = url.substring(index + 3).replaceAll("//", "/");

      url = url.substring(0, index + 3).concat(suffix);
    }

    // relative url
    else
    {
      url = url.replaceAll("//", "/");
    }

    return url;
  }

  /**
   * Method description
   *
   */
  @Override
  public void sendRequest(RepositoryHookEvent event)
  {
    if (configuration.isValid())
    {
      String urlSuffix = null;
      String type = repository.getType();

      if (logger.isDebugEnabled())
      {
        //J-
        logger.debug(
                "check for global jenkins hook: type={}, hg disabled={}, git disabled={}",
                type,
                configuration.isDisableMercurialTrigger(),
                configuration.isDisableGitTrigger()
        );
        //J+
      }

      if (TYPE_MERCURIAL.equalsIgnoreCase(type)
        &&!configuration.isDisableMercurialTrigger())
      {
        urlSuffix = URL_MERCURIAL;
      }
      else if (TYPE_GIT.equalsIgnoreCase(type)
        &&!configuration.isDisableGitTrigger())
      {
        urlSuffix = URL_GIT;
      }

      if (Util.isNotEmpty(urlSuffix))
      {
        String url = HttpUtil.getUriWithoutEndSeperator(
                       configuration.getUrl()).concat(urlSuffix);
        AdvancedHttpClient client = httpClientProvider.get();
        String repositoryUrl = createRepositoryUrl(repository);

        if (Util.isNotEmpty(repositoryUrl))
        {
          url = url.concat("?url=").concat(fixUrl(repositoryUrl));
        }

        try
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("try to access url {}", url);
          }

          AdvancedHttpResponse response = client.get(url).request();
          int statusCode = response.getStatus();

          if (logger.isInfoEnabled())
          {
            logger.info("request returned {}", statusCode);
          }
        }
        catch (IOException ex)
        {
          logger.error("could not execute http request", ex);
        }
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("repository type {} is not supported or is disabled", type);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("global configuration is not valid");
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private String createRepositoryUrl(Repository repository)
  {
    try (RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      return repositoryService
              .getSupportedProtocols()
              .filter(p -> "http".equals(p.getType()))
              .map(ScmProtocol::getUrl)
              .findFirst()
              .orElse(null);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GlobalJenkinsConfiguration configuration;

  /** Field description */
  private Provider<AdvancedHttpClient> httpClientProvider;

  /** Field description */
  private Repository repository;

  private final RepositoryServiceFactory repositoryServiceFactory;
}
