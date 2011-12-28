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

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpResponse;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHook;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jenkins post receive Hook.
 * This class is called after a changeset successfully pushed to a repository.
 * The jenkins hook checks if the hook is configured for this repository and
 * calls the configured jenkins ci server if a valid configuration is a
 * available. This class is a singleton, so be carefully.
 *
 * Extension objects in SCM-Manager:
 * https://bitbucket.org/sdorra/scm-manager/wiki/ExtensionPoints
 *
 * @author Sebastian Sdorra
 */
@Extension
public class JenkinsHook implements RepositoryHook
{

  /** Http parameter for jenkins authentication token */
  public static final String PARAMETER_TOKEN = "token";

  /** the logger for JenkinsHook */
  private static final Logger logger =
    LoggerFactory.getLogger(JenkinsHook.class);

  /** The type(s) of the repository hooks. */
  public static final Collection<RepositoryHookType> TYPES =
    Arrays.asList(RepositoryHookType.POST_RECEIVE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Creates a new instance of Jenkins Hook. This constructor is called by
   * Google Guice.Guice injects all necessary dependencies to the constructor.
   * For more informations on Google Guice contructor injection have a look
   * at http://code.google.com/p/google-guice/wiki/Injections.
   *
   * Available objects for injection in SCM-Manager:
   * https://bitbucket.org/sdorra/scm-manager/wiki/injectionObjects
   *
   * @param httpClientProvider Google Guice provider for an {@link HttpClient}
   */
  @Inject
  public JenkinsHook(Provider<HttpClient> httpClientProvider)
  {
    this.httpClientProvider = httpClientProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * This method is called after a changeset is successfully pushed to a
   * repository.
   *
   *
   * @param event the event of the hook. This object contains the changed
   *        repository and the changesets.
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {

    // get the changed repository
    Repository repository = event.getRepository();

    /**
     * check if the repository is not null. If the repository is null
     * log an error.
     */
    if (repository != null)
    {

      // read jenkins configuration from repository
      JenkinsConfiguration configuration = new JenkinsConfiguration(repository);

      // check if the configuration is valid and log error if not
      if (configuration.isValid())
      {
        handleRepositoryEvent(configuration);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("jenkins configuration for repository {} is not valid",
                    repository.getName());
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("receive repository hook without repository");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the types of the hook.
   *
   * @return a {@link Collection} of repository types.
   */
  @Override
  public Collection<RepositoryHookType> getTypes()
  {
    return TYPES;
  }

  /**
   * Return true if the hook can be executed asynchronous.
   * Note you can not access the current servlet or user if the hook is executed
   * asynchronous.
   *
   * @return true if the hook is executed asynchronous.
   */
  @Override
  public boolean isAsync()
  {
    return true;
  }

  //~--- methods --------------------------------------------------------------

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

  /** Guice provider for {@link HttpClient} */
  private Provider<HttpClient> httpClientProvider;
}
