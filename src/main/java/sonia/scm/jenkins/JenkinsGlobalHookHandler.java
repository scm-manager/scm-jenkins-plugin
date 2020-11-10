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
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.io.IOException;

import static sonia.scm.jenkins.Urls.fix;

public class JenkinsGlobalHookHandler implements JenkinsHookHandler {

  public static final String TYPE_GIT = "git";
  public static final String TYPE_MERCURIAL = "hg";
  public static final String URL_GIT = "/git/notifyCommit";
  public static final String URL_MERCURIAL = "/mercurial/notifyCommit";
  private static final Logger logger = LoggerFactory.getLogger(JenkinsGlobalHookHandler.class);

  private final GlobalJenkinsConfiguration configuration;
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory repositoryServiceFactory;

  public JenkinsGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                                  GlobalJenkinsConfiguration configuration, RepositoryServiceFactory repositoryServiceFactory) {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (configuration.isValid()) {
      String type = event.getRepository().getType();
      logGlobalJenkinsHook(type);

      String urlSuffix = createUrlSuffix(type);
      if (Util.isNotEmpty(urlSuffix)) {
        AdvancedHttpClient client = httpClientProvider.get();

        String url = createUrl(event.getRepository(), urlSuffix);
        sendRequest(client, url);
      } else {
        logger.warn("repository type {} is not supported or is disabled", type);
      }
    } else {
      logger.warn("global configuration is not valid");
    }
  }

  private void sendRequest(AdvancedHttpClient client, String url) {
    try {
      logger.debug("try to access url {}", url);
      AdvancedHttpResponse response = client.get(url).spanKind("Jenkins").request();
      int statusCode = response.getStatus();
      logger.info("request returned {}", statusCode);
    } catch (IOException ex) {
      logger.error("could not execute http request", ex);
    }
  }

  private void logGlobalJenkinsHook(String type) {
    //J-
    logger.debug(
      "check for global jenkins hook: type={}, hg disabled={}, git disabled={}",
      type,
      configuration.isDisableMercurialTrigger(),
      configuration.isDisableGitTrigger()
    );
    //J+
  }

  private String createUrl(Repository repository, String urlSuffix) {
    String url = HttpUtil.getUriWithoutEndSeperator(configuration.getUrl()).concat(urlSuffix);
    String repositoryUrl = createRepositoryUrl(repository);
    if (Util.isNotEmpty(repositoryUrl)) {
      url = url.concat("?url=").concat(fix(repositoryUrl));
    }
    return url;
  }

  private String createUrlSuffix(String type) {
    String urlSuffix = null;
    if (TYPE_MERCURIAL.equalsIgnoreCase(type)
      && !configuration.isDisableMercurialTrigger()) {
      urlSuffix = URL_MERCURIAL;
    } else if (TYPE_GIT.equalsIgnoreCase(type)
      && !configuration.isDisableGitTrigger()) {
      urlSuffix = URL_GIT;
    }
    return urlSuffix;
  }

  private String createRepositoryUrl(Repository repository) {
    try (RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      return repositoryService
        .getSupportedProtocols()
        .filter(p -> "http".equals(p.getType()))
        .map(ScmProtocol::getUrl)
        .findFirst()
        .orElse(null);
    }
  }
}
