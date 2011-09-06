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
 *
 * @author Sebastian Sdorra
 */
@Extension
public class JenkinsHook implements RepositoryHook
{

  /** Field description */
  public static final String PARAMETER_TOKEN = "token";

  /** Field description */
  public static final String PROPERTY_JENKINS_PROJECT = "jenkins.project";

  /** Field description */
  public static final String PROPERTY_JENKINS_TOKEN = "jenkins.token";

  /** Field description */
  public static final String PROPERTY_JENKINS_URL = "jenkins.url";

  /** the logger for JenkinsHook */
  private static final Logger logger =
    LoggerFactory.getLogger(JenkinsHook.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param httpClientProvider
   */
  @Inject
  public JenkinsHook(Provider<HttpClient> httpClientProvider)
  {
    this.httpClientProvider = httpClientProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Override
  public void onEvent(RepositoryHookEvent event)
  {
    Repository repository = event.getRepository();

    if (repository != null)
    {
      handleRepositoryEvent(repository);
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("receive repository hook without repository");
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   *   Method description
   *
   *
   *   @return
   */
  @Override
  public Collection<RepositoryHookType> getTypes()
  {
    return Arrays.asList(RepositoryHookType.POST_RECEIVE);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAsync()
  {
    return true;
  }

  //~--- methods --------------------------------------------------------------

  /**
   *
   *
   * @param url
   * @param project
   *
   * @return
   */
  private String createUrl(String url, String project)
  {
    if (!url.endsWith("/"))
    {
      url = url.concat("/");
    }

    return url.concat("job/").concat(project).concat("/build");
  }

  /**
   * Method description
   *
   *
   * @param repository
   */
  private void handleRepositoryEvent(Repository repository)
  {
    String url = repository.getProperty(PROPERTY_JENKINS_URL);
    String project = repository.getProperty(PROPERTY_JENKINS_PROJECT);

    if (Util.isNotEmpty(url) && Util.isNotEmpty(project))
    {
      url = createUrl(url, project);

      if (logger.isInfoEnabled())
      {
        logger.info("call jenkins at {}", url);
      }

      String token = repository.getProperty(PROPERTY_JENKINS_TOKEN);

      try
      {
        sendRequest(url, token);
      }
      catch (IOException ex)
      {
        logger.error("could not send request to jenkins", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param token
   *
   * @throws IOException
   */
  private void sendRequest(String url, String token) throws IOException
  {
    HttpResponse response = null;
    HttpClient httpClient = httpClientProvider.get();

    if (Util.isNotEmpty(token))
    {
      Map<String, List<String>> parameters = new HashMap<String,
                                               List<String>>();

      parameters.put(PARAMETER_TOKEN, Arrays.asList(token));
      response = httpClient.post(url, parameters);
    }
    else
    {
      response = httpClient.post(url);
    }

    int sc = response.getStatusCode();

    if (sc >= 400)
    {
      logger.error("jenkins returned status code {}", sc);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<HttpClient> httpClientProvider;
}
