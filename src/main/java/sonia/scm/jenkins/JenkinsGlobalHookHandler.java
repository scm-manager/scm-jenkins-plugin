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

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
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

abstract class JenkinsGlobalHookHandler implements JenkinsHookHandler {

  private static final Logger logger = LoggerFactory.getLogger(JenkinsGlobalHookHandler.class);

  private final GlobalJenkinsConfiguration configuration;
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory repositoryServiceFactory;

  JenkinsGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
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

      String urlSuffix = createUrlSuffix();
      if (Util.isNotEmpty(urlSuffix)) {
        AdvancedHttpClient client = httpClientProvider.get();

        String url = createUrl(urlSuffix);
        sendRequest(client, url, event.getRepository());
      } else {
        logger.warn("repository type {} is not supported or is disabled", type);
      }
    } else {
      logger.warn("global configuration is not valid");
    }
  }

  private void sendRequest(AdvancedHttpClient client, String url, Repository repository) {
    try {
      logger.info("try to access url {}", url);
      AdvancedHttpRequest request = client.get(url).spanKind("Jenkins");
      addQueryParameters(repository, request);
      if (Util.isNotEmpty(configuration.getUsername())) {
        HeaderAppenders.appendCsrfCrumbHeader(client, request, configuration.getUrl(), configuration.getUsername(), configuration.getApiToken());
      }
      AdvancedHttpResponse response = request.request();
      int statusCode = response.getStatus();
      logger.info("request returned {}", statusCode);
    } catch (IOException ex) {
      logger.error("could not execute http request", ex);
    }
  }

  void addQueryParameters(Repository repository, AdvancedHttpRequest request) {
    String repositoryUrl = createRepositoryUrl(repository);
    if (Util.isNotEmpty(repositoryUrl)) {
      request.queryString("url", fix(repositoryUrl));
    }
  }

  private void logGlobalJenkinsHook(String type) {
    //J-
    logger.debug(
      "check for global jenkins hook: type={}, hg disabled={}, git disabled={}, svn disabled={}",
      type,
      configuration.isDisableMercurialTrigger(),
      configuration.isDisableGitTrigger(),
      configuration.isDisableSubversionTrigger()
    );
    //J+
  }

  private String createUrl(String urlSuffix) {
    return HttpUtil.getUriWithoutEndSeperator(configuration.getUrl()).concat(urlSuffix);
  }

  abstract String createUrlSuffix();

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
