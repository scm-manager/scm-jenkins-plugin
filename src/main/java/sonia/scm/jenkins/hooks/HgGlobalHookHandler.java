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

import com.google.common.base.Strings;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.GlobalJenkinsConfiguration;
import sonia.scm.jenkins.JenkinsHookHandler;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import static sonia.scm.jenkins.hooks.Urls.fix;

public class HgGlobalHookHandler implements JenkinsHookHandler {

  private static final Logger LOG = LoggerFactory.getLogger(HgGlobalHookHandler.class);

  public static final String TYPE = "hg";
  public static final String URL_MERCURIAL = "/mercurial/notifyCommit";
  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final RepositoryServiceFactory serviceFactory;
  private final GlobalJenkinsConfiguration configuration;

  public HgGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                             RepositoryServiceFactory serviceFactory,
                             GlobalJenkinsConfiguration configuration) {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
    this.serviceFactory = serviceFactory;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (configuration.isValid() && !configuration.isDisableMercurialTrigger()) {
      String type = event.getRepository().getType();
      LOG.debug(
        "check for global jenkins hook: type={}, hg disabled={}",
        type,
        configuration.isDisableMercurialTrigger()
      );

        AdvancedHttpClient client = httpClientProvider.get();
        String url = createUrl(event.getRepository());
        GlobalHookUtils.sendRequest(configuration, client, url);
    } else {
      LOG.warn("global configuration is not valid");
    }
  }

  String createUrl(Repository repository) {
    String url = HttpUtil.getUriWithoutEndSeperator(configuration.getUrl()).concat(URL_MERCURIAL);
    String repositoryUrl = createRepositoryUrl(repository);
    if (!Strings.isNullOrEmpty(configuration.getApiToken())) {
      url = url + "?token=" + configuration.getApiToken();
    }
    if (Util.isNotEmpty(repositoryUrl)) {
      url = url.concat("&url=").concat(fix(repositoryUrl));
    }
    return url;
  }

  String createRepositoryUrl(Repository repository) {
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      return repositoryService
        .getSupportedProtocols()
        .filter(p -> "http".equals(p.getType()))
        .map(ScmProtocol::getUrl)
        .findFirst()
        .orElse(null);
    }
  }
}
