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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.List;
import java.util.Set;
import sonia.scm.util.IOUtil;

import javax.inject.Provider;

/**
 *
 * @author Sebastian Sdorra
 */
public class JenkinsRepositoryHookHandler implements JenkinsHookHandler
{

  /** Http parameter for jenkins authentication token */
  public static final String PARAMETER_TOKEN = "token";

  /** the logger for JenkinsRepositoryHookHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(JenkinsRepositoryHookHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param httpClientProvider
   * @param configuration
   */
  public JenkinsRepositoryHookHandler(Provider<AdvancedHttpClient> httpClientProvider,
                                      JenkinsConfiguration configuration)
  {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void sendRequest(RepositoryHookEvent event)
  {
    if (configuration.getBranches().isEmpty())
    {
      logger.debug("branch list is empty, send request");
      handleRepositoryEvent(configuration);
    }
    else
    {
      if (isInBranchSet(configuration.getBranches(), event.getContext().getChangesetProvider().getChangesets()))
      {
        handleRepositoryEvent(configuration);
      }
      else
      {
        logger.debug("changesets does not contain configured branches");
      }
    }
  }
  
  private String createBaseUrl(JenkinsConfiguration configuration){
    String url = configuration.getUrl();

    if (!url.endsWith("/"))
    {
      url = url.concat("/");
    }
    
    return url;
  }

  /**
   * Creates the url to the remote trigger servlet of jenkins. The method is
   * visible to the package for testing.
   *
   * @param configuration jenkins configuration
   *
   * @return the url to the remote trigger servlet of jenkins
   */
  String createUrl(JenkinsConfiguration configuration)
  {
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
  String escape(String urlString)
  {
    String escapedUrl = urlString;

    try
    {
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
    }
    catch (URISyntaxException ex)
    {
      logger.warn("could not escaped url", ex);
    }
    catch (MalformedURLException ex)
    {
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
  private void handleRepositoryEvent(JenkinsConfiguration configuration)
  {

    // create the url to the endpoint
    String url = createUrl(configuration);

    // log the url
    if (logger.isInfoEnabled())
    {
      logger.info("call jenkins at {}", url);
    }

    try
    {

      // send the request to the jenkins server
      sendRequest(configuration, url);
    }

    // catch each IOException and log them
    catch (IOException ex)
    {
      logger.error("could not send request to jenkins", ex);
    }
  }
  
  private String createCrumbUrl(JenkinsConfiguration configuration)
  {
    return escape(createBaseUrl(configuration).concat("crumbIssuer/api/xml"));
  }
  
  private CsrfCrumb parseCsrfCrumbResponse(AdvancedHttpResponse response) throws IOException
  {
    CsrfCrumb csrfCrumb = null;
    InputStream content = null;
    try 
    {
      content = response.contentAsStream();
      csrfCrumb = CsrfCrumbParser.parse(content);
    } 
    finally 
    {
      IOUtil.close(content);
    }
    return csrfCrumb;
  }
  
  private CsrfCrumb getJenkinsCsrfCrumb(JenkinsConfiguration configuration, AdvancedHttpClient client)
  {
    String url = createCrumbUrl(configuration);
    logger.debug("fetch csrf crumb from {}", url);

    AdvancedHttpRequest request = client.get(url);

    appendAuthenticationHeader(configuration, request);
    
    CsrfCrumb crumb = null;
    try
    {
      AdvancedHttpResponse response = request.request();
      int sc = response.getStatus();
      if (sc != 200)
      {
        logger.warn("jenkins crumb endpoint returned status code {}", sc);
      } 
      else 
      {
        crumb = parseCsrfCrumbResponse(response);
      }
    }
    catch (IOException ex)
    {
      logger.warn("failed to fetch csrf crumb", ex);
    }
    
    return crumb;
  }
  
  private void appendCsrfCrumb(JenkinsConfiguration configuration, AdvancedHttpClient client, BaseHttpRequest request)
  {
    if (configuration.isCsrf())
    {
      CsrfCrumb crumb = getJenkinsCsrfCrumb(configuration, client);
      logger.debug("add csrf crumb to api request");
      request.header(crumb.getCrumbRequestField(), crumb.getCrumb());
    }
    else 
    {
      logger.debug("csrf protection is disabled, skipping crumb");
    }
  }
  
  private void appendAuthenticationHeader(JenkinsConfiguration configuration, BaseHttpRequest request){
    // check for authentication parameters
    String username = configuration.getUsername();
    String apiToken = configuration.getApiToken();

    if (Util.isNotEmpty(username) && Util.isNotEmpty(apiToken))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("added authentication for user {}", username);
      }

      // add basic authentication header
      request.basicAuth(username, apiToken);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("skip authentication. username or api token is empty");
    }
  }

  /**
   * Send the request to the jenkins ci server to trigger a new build.
   *
   * @param configuration jenkins configuration
   * @param url url of the jenkins server
   *
   * @throws IOException
   */
  private void sendRequest(JenkinsConfiguration configuration, String url)
    throws IOException
  {

    /**
     * Create a new http client from the Guice Provider.
     */
    AdvancedHttpClient httpClient = httpClientProvider.get();

    // retrive authentication token
    AdvancedHttpRequestWithBody request = httpClient.post(url);
    String token = configuration.getToken();
    // check if the token is not empty.
    if (Util.isNotEmpty(token))
    {

      // add the token as parameter for the request
      request.queryString(PARAMETER_TOKEN, token);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("no project token is available");
    }

    appendAuthenticationHeader(configuration, request);
    appendCsrfCrumb(configuration, httpClient, request);

    /**
     * execute the http post request with the http client and
     * fetch the status code of the response.
     */
    int sc = request.request().getStatus();

    // if the response is greater than 400 write an error to the log
    if (sc >= 400)
    {
      logger.error("jenkins returned status code {}", sc);
    }
    else if (logger.isInfoEnabled())
    {
      logger.info("jenkins hook successfully submited");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param branchSet
   * @param changesets
   *
   * @return
   */
  private boolean isInBranchSet(Set<String> branchSet,
    Iterable<Changeset> changesets)
  {
    boolean found = false;

    for (Changeset changeset : changesets)
    {
      if (isInBranchSet(branchSet, changeset))
      {
        found = true;

        break;
      }
    }

    return found;
  }

  /**
   * Method description
   *
   *
   * @param branchSet
   * @param changeset
   *
   * @return
   */
  private boolean isInBranchSet(Set<String> branchSet, Changeset changeset)
  {
    boolean found = false;
    List<String> branches = changeset.getBranches();

    if (Util.isNotEmpty(branches))
    {
      for (String branch : branches)
      {
        if (branchSet.contains(branch))
        {
          logger.debug("found branch {} at {}, send request", branch,
            changeset.getId());
          found = true;

          break;
        }
      }
    }

    return found;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JenkinsConfiguration configuration;

  /** Guice provider for {@link AdvancedHttpClient} */
  private Provider<AdvancedHttpClient> httpClientProvider;
}
