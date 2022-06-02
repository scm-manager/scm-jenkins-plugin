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

import com.google.common.base.Strings;
import com.google.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.LookupCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

import static sonia.scm.jenkins.HeaderAppenders.appendCsrfCrumbHeader;

@Slf4j
public class JenkinsSvnGlobalHookHandler implements JenkinsHookHandler {

  // This is the endpoint we trigger in Jenkins Subversion-Plugin:
  // https://github.com/jenkinsci/subversion-plugin/blob/master/src/main/java/hudson/scm/SubversionRepositoryStatus.java#L92
  public static final String URL_SUBVERSION = "/subversion/{0}/notifyCommit/?rev={1}";
  public static final String TYPE_SUBVERSION = "svn";

  private final Provider<AdvancedHttpClient> httpClientProvider;
  private final GlobalJenkinsConfiguration configuration;
  private final RepositoryServiceFactory repositoryServiceFactory;

  public JenkinsSvnGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider, GlobalJenkinsConfiguration configuration, RepositoryServiceFactory repositoryServiceFactory) {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (Util.isNotEmpty(configuration.getUrl()) && !configuration.isDisableSubversionTrigger()) {
      try (RepositoryService repositoryService = repositoryServiceFactory.create(event.getRepository())) {
        Optional<String> uuid = lookupUUID(repositoryService);
        if (uuid.isPresent()) {
          log.debug("Lookup for svn repository uuid: {}", uuid.get());
          String url = createUrl(event, uuid.get());
          String content = getContent(event);
          sendRequest(url, content);
        } else {
          log.error("Could not send request: No uuid for svn repository found");
        }
      }
    }
  }

  private void sendRequest(String url, String content) {
    try {
      AdvancedHttpRequestWithBody request = httpClientProvider.get()
        .post(url)
        .spanKind("Jenkins")
        .basicAuth(configuration.getUsername(), configuration.getApiToken())
        .header("Content-Type", "text/plain;charset=UTF-8")
        .stringContent(content);


      if (doesGlobalConfigProvideCredentials()) {
        appendCsrfCrumbHeader(httpClientProvider.get(), request, configuration.getUrl(), configuration.getUsername(), configuration.getApiToken());
      }

      AdvancedHttpResponse response = request.request();

      int statusCode = response.getStatus();

      if (log.isInfoEnabled()) {
        log.info("request returned {}", statusCode);
      }
    } catch (IOException e) {
      log.error("could not execute http request", e);
    }
  }

  private boolean doesGlobalConfigProvideCredentials() {
    return !Strings.isNullOrEmpty(configuration.getUsername()) && !Strings.isNullOrEmpty(configuration.getApiToken());
  }

  private String getContent(RepositoryHookEvent event) {
    StringBuilder content = new StringBuilder();
    try (RepositoryService service = repositoryServiceFactory.create(event.getRepository())) {
      String revision = event.getContext().getChangesetProvider().getChangesetList().get(0).getId();
      Modifications modifications = service.getModificationsCommand().revision(revision).getModifications();
      addModificationsToContent(content, modifications);
    } catch (IOException e) {
      log.error("Could not find modifications for changeset" + e);
    }
    return content.toString();
  }

  private void addModificationsToContent(StringBuilder content, Modifications modifications) {
    modifications.getAdded().forEach(m -> content.append("A").append("   ").append(m.getPath()).append("\n"));
    modifications.getCopied().forEach(m -> content.append("A").append("   ").append(m.getTargetPath()).append("\n"));
    modifications.getModified().forEach(m -> content.append("U").append("   ").append(m.getPath()).append("\n"));
    modifications.getRenamed().forEach(m -> content.append("U").append("   ").append(m.getNewPath()).append("\n"));
    modifications.getRemoved().forEach(m -> content.append("D").append("   ").append(m.getPath()).append("\n"));
  }

  private String createUrl(RepositoryHookEvent event, String uuid) {
    String revision = event.getContext().getChangesetProvider().getChangesetList().get(0).getId();
    String urlSuffix = MessageFormat.format(URL_SUBVERSION, uuid, revision);
    return HttpUtil.getUriWithoutEndSeperator(configuration.getUrl()).concat(urlSuffix);
  }

  private Optional<String> lookupUUID(RepositoryService repositoryService) {
    LookupCommandBuilder command = repositoryService.getLookupCommand();
    return command.lookup(String.class, "propget", "uuid", "/");
  }
}
