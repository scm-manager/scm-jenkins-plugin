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

import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpResponse;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public JenkinsRepositoryHookHandler(Provider<HttpClient> httpClientProvider,
          JenkinsConfiguration configuration)
  {
    this.httpClientProvider = httpClientProvider;
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void sendRequest()
  {
    handleRepositoryEvent(configuration);
  }

  /**
   * Creates the url to the remote trigger servlet of jenkins.
   *
   * @param configuration jenkins configuration
   *
   * @return the url to the remote trigger servlet of jenkins
   */
  private String createUrl(JenkinsConfiguration configuration)
  {
    String url = configuration.getUrl();

    if (!url.endsWith("/"))
    {
      url = url.concat("/");
    }

    return url.concat("job/").concat(configuration.getProject()).concat(
        "/build");
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
    HttpClient httpClient = httpClientProvider.get();
    HttpResponse response = null;

    // retrive authentication token
    String token = configuration.getToken();

    // check if the token is not empty.
    if (Util.isNotEmpty(token))
    {

      // add the token as parameter for the request
      Map<String, List<String>> parameters = new HashMap<String,
                                               List<String>>();

      parameters.put(PARAMETER_TOKEN, Arrays.asList(token));

      // execute the http post request with the http client
      response = httpClient.post(url, parameters);
    }
    else
    {

      // execute the http post request with the http client
      response = httpClient.post(url);
    }

    // fetch the status code of the response
    int sc = response.getStatusCode();

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JenkinsConfiguration configuration;

  /** Guice provider for {@link HttpClient} */
  private Provider<HttpClient> httpClientProvider;
}
