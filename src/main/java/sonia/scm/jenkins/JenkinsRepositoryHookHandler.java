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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author Sebastian Sdorra
 */
public class JenkinsRepositoryHookHandler implements JenkinsHookHandler {

  public static final String PARAMETER_TOKEN = "token";
  private static final Logger logger = LoggerFactory.getLogger(JenkinsRepositoryHookHandler.class);

  private final JenkinsConfiguration configuration;
  private final Provider<AdvancedHttpClient> httpClientProvider;

  public JenkinsRepositoryHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                                      JenkinsConfiguration configuration) {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
  }

  @Override
  public void sendRequest(RepositoryHookEvent event) {
    if (configuration.getBranches().isEmpty()) {
      logger.debug("branch list is empty, send request");
      handleRepositoryEvent(configuration);
    } else {
      if (isInBranchSet(configuration.getBranches(), event.getContext().getChangesetProvider().getChangesets())) {
        handleRepositoryEvent(configuration);
      } else {
        logger.debug("changesets does not contain configured branches");
      }
    }
  }

  private String createBaseUrl(JenkinsConfiguration configuration) {
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
  String createUrl(JenkinsConfiguration configuration) {
    String url = createBaseUrl(configuration);

    //J-
    // url encode proejct name, see http://goo.gl/v8Rond
    return escape(
      url.concat("job/")
        .concat(configuration.getProject())
        .concat("/build")
    );
    //J+
  }

  /**
   * Escapes a complete url. The method is visible to the package for testing.
   *
   * @param urlString url
   * @return escaped url
   */
  String escape(String urlString) {
    String escapedUrl = urlString;

    try {
      URL url = new URL(urlString);
      //J-
      URI uri = new URI(
        url.getProtocol(),
        url.getUserInfo(),
        url.getHost(),
        url.getPort(),
        url.getPath(),
        url.getQuery(),
        url.getRef()
      );
      //J+

      escapedUrl = uri.toString();
    } catch (URISyntaxException | MalformedURLException ex) {
      logger.warn("could not escaped url", ex);
    }

    return escapedUrl;
  }

  /**
   * Handles the repository event. Checks the configuration for the repository
   * and calls the jenkins server.
   *
   * @param configuration jenkins configuration
   */
  private void handleRepositoryEvent(JenkinsConfiguration configuration) {

    String url = createUrl(configuration);

    if (logger.isInfoEnabled()) {
      logger.info("call jenkins at {}", url);
    }

    try {
      sendRequest(configuration, url);
    } catch (IOException ex) {
      logger.error("could not send request to jenkins", ex);
    }
  }

  private String createCrumbUrl(JenkinsConfiguration configuration) {
    return escape(createBaseUrl(configuration).concat("crumbIssuer/api/xml"));
  }

  private CsrfCrumb parseCsrfCrumbResponse(AdvancedHttpResponse response) throws IOException {
    CsrfCrumb csrfCrumb;
    InputStream content = null;
    try {
      content = response.contentAsStream();
      csrfCrumb = CsrfCrumbParser.parse(content);
    } finally {
      IOUtil.close(content);
    }
    return csrfCrumb;
  }

  private CsrfCrumb getJenkinsCsrfCrumb(JenkinsConfiguration configuration, AdvancedHttpClient client) {
    String url = createCrumbUrl(configuration);
    logger.debug("fetch csrf crumb from {}", url);

    AdvancedHttpRequest request = client.get(url);

    appendAuthenticationHeader(configuration, request);

    CsrfCrumb crumb = null;
    try {
      AdvancedHttpResponse response = request.request();
      int sc = response.getStatus();
      if (sc != 200) {
        logger.warn("jenkins crumb endpoint returned status code {}", sc);
      } else {
        crumb = parseCsrfCrumbResponse(response);
      }
    } catch (IOException ex) {
      logger.warn("failed to fetch csrf crumb", ex);
    }

    return crumb;
  }

  private void appendCsrfCrumb(JenkinsConfiguration configuration, AdvancedHttpClient client, BaseHttpRequest request) {
    if (configuration.isCsrf()) {
      CsrfCrumb crumb = getJenkinsCsrfCrumb(configuration, client);
      if (crumb != null) {
        logger.debug("add csrf crumb to api request");
        request.header(crumb.getCrumbRequestField(), crumb.getCrumb());
      }
    } else {
      logger.debug("csrf protection is disabled, skipping crumb");
    }
  }

  private void appendAuthenticationHeader(JenkinsConfiguration configuration, BaseHttpRequest request) {
    // check for authentication parameters
    String username = configuration.getUsername();
    String apiToken = configuration.getApiToken();

    if (Util.isNotEmpty(username) && Util.isNotEmpty(apiToken)) {
      if (logger.isDebugEnabled()) {
        logger.debug("added authentication for user {}", username);
      }

      // add basic authentication header
      request.basicAuth(username, apiToken);
    } else if (logger.isDebugEnabled()) {
      logger.debug("skip authentication. username or api token is empty");
    }
  }

  /**
   * Send the request to the jenkins ci server to trigger a new build.
   *
   * @param configuration jenkins configuration
   * @param url           url of the jenkins server
   * @throws IOException
   */
  private void sendRequest(JenkinsConfiguration configuration, String url)
    throws IOException {

    /**
     * Create a new http client from the Guice Provider.
     */
    AdvancedHttpClient httpClient = httpClientProvider.get();
    url = appendBuildParameters(configuration, url);

    // retrive authentication token
    AdvancedHttpRequestWithBody request = httpClient.post(url);
    String token = configuration.getToken();
    // check if the token is not empty.
    if (Util.isNotEmpty(token)) {

      // add the token as parameter for the request
      request.queryString(PARAMETER_TOKEN, token);
    } else if (logger.isDebugEnabled()) {
      logger.debug("no project token is available");
    }

    appendAuthenticationHeader(configuration, request);
    appendCsrfCrumb(configuration, httpClient, request);

    /**
     * execute the http post request with the http client and
     * fetch the status code of the response.
     */
    int sc = request.request().getStatus();

    if (sc >= 400) {
      logger.error("jenkins returned status code {}", sc);
    } else if (logger.isInfoEnabled()) {
      logger.info("jenkins hook successfully submitted");
    }
  }

  private String appendBuildParameters(JenkinsConfiguration configuration, String url) {
    Set<BuildParameter> buildParameters = configuration.getBuildParameters();

    if (!buildParameters.isEmpty()) {
      StringBuilder builder = new StringBuilder(url);
      builder.append("WithParameters?");
      buildParameters.forEach(
        parameter -> builder
          .append(HttpUtil.encode(parameter.getName()))
          .append("=")
          .append(HttpUtil.encode(parameter.getValue()))
          .append("&")
      );
      url = builder.toString().substring(0, builder.toString().length() - 1);
    }

    return url;
  }

  private boolean isInBranchSet(Set<String> branchSet,
                                Iterable<Changeset> changesets) {
    for (Changeset changeset : changesets) {
      if (isInBranchSet(branchSet, changeset)) {
        return true;
      }
    }
    return false;
  }

  private boolean isInBranchSet(Set<String> branchSet, Changeset changeset) {
    List<String> branches = changeset.getBranches();

    if (Util.isNotEmpty(branches)) {
      for (String branch : branches) {
        if (branchSet.contains(branch)) {
          logger.debug("found branch {} at {}, send request", branch, changeset.getId());
          return true;
        }
      }
    }

    return false;
  }
}
