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

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.LookupCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.Optional;

public class JenkinsGlobalHookHandler implements JenkinsHookHandler {

  public static final String TYPE_GIT = "git";
  public static final String TYPE_MERCURIAL = "hg";
  public static final String TYPE_SUBVERSION = "svn";
  public static final String URL_GIT = "/git/notifyCommit";
  public static final String URL_MERCURIAL = "/mercurial/notifyCommit";
  public static final String URL_SUBVERSION = "/subversion/{UUID}/notifyCommit";
  private static final Logger logger = LoggerFactory.getLogger(JenkinsGlobalHookHandler.class);


  private final GlobalJenkinsConfiguration configuration;
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final Repository repository;
  private final RepositoryServiceFactory repositoryServiceFactory;


  public JenkinsGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                                  GlobalJenkinsConfiguration configuration, Repository repository, RepositoryServiceFactory repositoryServiceFactory) {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
    this.repository = repository;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  /**
   * Fix duplicated slashes in repository url.
   *
   * @param url url to fix
   * @return fixed url
   * @see <a target="_blank" href="https://bitbucket.org/sdorra/scm-manager/issue/510/wrong-git-notification-url-to-many-slashes">Issue 510</a>
   */
  static String fixUrl(String url) {
    int index = url.indexOf("://");

    // absolute url
    if (index > 0) {
      String suffix = url.substring(index + 3).replaceAll("//", "/");

      url = url.substring(0, index + 3).concat(suffix);
    }

    // relative url
    else {
      url = url.replaceAll("//", "/");
    }

    return url;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (configuration.isValid()) {
      String urlSuffix = null;
      String type = repository.getType();

      if (logger.isDebugEnabled()) {
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
        && !configuration.isDisableMercurialTrigger()) {
        urlSuffix = URL_MERCURIAL;
      } else if (TYPE_GIT.equalsIgnoreCase(type)
        && !configuration.isDisableGitTrigger()) {
        urlSuffix = URL_GIT;
      } else if (isSvnRepository(repository) && !configuration.isDisableSubversionTrigger()) {
        try (RepositoryService repositoryService = repositoryServiceFactory.create(event.getRepository())) {
          Optional<String> uuid = lookupUUID(repositoryService);
          if (uuid.isPresent()) {
            logger.debug("Lookup for svn repository uuid: {}", uuid.get());
            urlSuffix = URL_SUBVERSION.replace("{UUID}", uuid.get());
          }
        }
      }

      if (Util.isNotEmpty(urlSuffix)) {
        String url = HttpUtil.getUriWithoutEndSeperator(
          configuration.getUrl()).concat(urlSuffix);
        AdvancedHttpClient client = httpClientProvider.get();
        String repositoryUrl = createRepositoryUrl(repository);

        if (Util.isNotEmpty(repositoryUrl)) {
          url = url.concat("?url=").concat(fixUrl(repositoryUrl));
        }

        try {
          if (logger.isDebugEnabled()) {
            logger.debug("try to access url {}", url);
          }

          AdvancedHttpResponse response;
            if (isSvnRepository(repository)) {
              response = client.post(url).request();
            } else {
              response = client.get(url).request();
            }
          int statusCode = response.getStatus();

          if (logger.isInfoEnabled()) {
            logger.info("request returned {}", statusCode);
          }
        } catch (IOException ex) {
          logger.error("could not execute http request", ex);
        }
      } else if (logger.isWarnEnabled()) {
        logger.warn("repository type {} is not supported or is disabled", type);
      }
    } else if (logger.isWarnEnabled()) {
      logger.warn("global configuration is not valid");
    }
  }

  private boolean isSvnRepository(Repository repository) {
    return TYPE_SUBVERSION.equalsIgnoreCase(repository.getType());
  }

  private String createRepositoryUrl(Repository repository) {
    if (isSvnRepository(repository)) {
      return "";
    }
    try (RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      return repositoryService
        .getSupportedProtocols()
        .filter(p -> "http".equals(p.getType()))
        .map(ScmProtocol::getUrl)
        .findFirst()
        .orElse(null);
    }
  }

  private Optional<String> lookupUUID(RepositoryService repositoryService) {
    LookupCommandBuilder command = repositoryService.getLookupCommand();
    return command.lookup(String.class, "props", "/", "uuid");
  }
}
