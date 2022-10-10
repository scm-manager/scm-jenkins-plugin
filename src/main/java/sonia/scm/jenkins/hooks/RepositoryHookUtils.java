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

import com.cloudogu.scm.el.ElParser;
import com.cloudogu.scm.el.env.ImmutableEncodedChangeset;
import com.cloudogu.scm.el.env.ImmutableEncodedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.jenkins.BuildParameter;
import sonia.scm.jenkins.JenkinsConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static sonia.scm.jenkins.hooks.HeaderAppenders.appendAuthenticationHeader;
import static sonia.scm.jenkins.hooks.HeaderAppenders.appendCsrfCrumbHeader;
import static sonia.scm.jenkins.hooks.Urls.escape;

public class RepositoryHookUtils {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryHookUtils.class);


  private RepositoryHookUtils() {
  }

  static String createBaseUrl(JenkinsConfiguration configuration) {
    String url = configuration.getUrl();

    if (!url.endsWith("/")) {
      url = url.concat("/");
    }

    return url;
  }

  /**
   * Creates the url to the remote trigger servlet of jenkins. The method is
   * visible to the package for testing.
   *
   * @param configuration jenkins configuration
   * @return the url to the remote trigger servlet of jenkins
   */
  static String createUrl(JenkinsConfiguration configuration) {
    String url = createBaseUrl(configuration);

    //J-
    // url encode project name, see http://goo.gl/v8Rond
    return escape(
      url.concat("job/")
        .concat(configuration.getProject())
        .concat("/build")
    );
    //J+
  }

  /**
   * Handles the repository event. Checks the configuration for the repository
   * and calls the jenkins server.
   *
   * @param configuration jenkins configuration
   */
  static void handleRepositoryEvent(Provider<AdvancedHttpClient> httpClientProvider, ElParser elParser, JenkinsConfiguration configuration, String url, RepositoryHookEvent event) {

    if (LOG.isInfoEnabled()) {
      LOG.info("call jenkins at {}", url);
    }

    try {
      sendRequest(httpClientProvider, elParser, configuration, event, url);
    } catch (IOException ex) {
      LOG.error("could not send request to jenkins", ex);
    }
  }

  /**
   * Send the request to the jenkins ci server to trigger a new build.
   *
   * @param configuration jenkins configuration
   * @param url           url of the jenkins server
   * @throws IOException
   */
  static void sendRequest(Provider<AdvancedHttpClient> httpClientProvider, ElParser elParser, JenkinsConfiguration configuration, RepositoryHookEvent event, String url)
    throws IOException {

    AdvancedHttpClient httpClient = httpClientProvider.get();
    url = appendBuildParameters(elParser, configuration, event, url);
    AdvancedHttpRequestWithBody request = httpClient.post(url).spanKind("Jenkins");
    appendAuthenticationHeader(request, configuration.getUsername(), configuration.getApiToken());
    appendCsrfCrumbHeader(httpClient, request, configuration.getUrl(), configuration.getUsername(), configuration.getApiToken());

    AdvancedHttpResponse response = request.request();
    logResult(response);
  }

  private static void logResult(AdvancedHttpResponse response) {
    int sc = response.getStatus();
    if (sc >= 400) {
      LOG.error("jenkins returned status code {}", sc);
    } else if (LOG.isInfoEnabled()) {
      LOG.info("jenkins hook successfully submitted");
    }
  }

  static boolean hasChangeset(RepositoryHookEvent event) {
    return event.getContext() != null
      && event.getContext().getChangesetProvider().getChangesets() != null
      && event.getContext().getChangesetProvider().getChangesets().iterator().hasNext();
  }

  static String appendBuildParameters(ElParser elParser, JenkinsConfiguration configuration, RepositoryHookEvent event, String url) {
    Set<BuildParameter> buildParameters = configuration.getBuildParameters();

    Map<String, Object> env = new HashMap<>();

    env.put("repository", new ImmutableEncodedRepository(event.getRepository()));

    if (hasChangeset(event)) {
      ImmutableEncodedChangeset iec = new ImmutableEncodedChangeset(
        event.getContext().getChangesetProvider().getChangesets().iterator().next()
      );
      env.put("changeset", iec);
      env.put("commit", iec);
    }

    if (!buildParameters.isEmpty()) {
      StringBuilder builder = new StringBuilder(url);
      builder.append("WithParameters?");
      buildParameters.forEach(
        parameter -> builder
          .append(HttpUtil.encode(parameter.getName()))
          .append("=")
          // First decode before encode to avoid parsing errors from jexl like "@"
          .append(HttpUtil.encode(HttpUtil.decode(elParser.parse(parameter.getValue()).evaluate(env))))
          .append("&")
      );
      url = builder.substring(0, builder.length() - 1);
    }

    return url;
  }

  static boolean isInBranchSet(Set<String> branchSet,
                               Iterable<Changeset> changesets) {
    for (Changeset changeset : changesets) {
      if (isInBranchSet(branchSet, changeset)) {
        return true;
      }
    }
    return false;
  }

  static boolean isInBranchSet(Set<String> branchSet, Changeset changeset) {
    List<String> branches = changeset.getBranches();

    if (Util.isNotEmpty(branches)) {
      for (String branch : branches) {
        if (branchSet.contains(branch)) {
          LOG.debug("found branch {} at {}, send request", branch, changeset.getId());
          return true;
        }
      }
    }

    return false;
  }
}
