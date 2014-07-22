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

import sonia.scm.Validateable;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for Jenkins Hook. This configuration reads the properties from
 * the repository. The properties can be configured from web interface.
 *
 * @author Sebastian Sdorra
 */
public class JenkinsConfiguration implements Validateable
{

  /** Repository property for the api token of the user */
  public static final String PROPERTY_JENKINS_APITOKEN = "jenkins.api-token";

  /** Repository property for branch restrictions */
  public static final String PROPERTY_JENKINS_BRANCHES = "jenkins.branches";

  /** Repository property for a comma seperated list of project keys */
  public static final String PROPERTY_JENKINS_PROJECT = "jenkins.project";

  /** Repository property for the jenkins authentication token */
  public static final String PROPERTY_JENKINS_TOKEN = "jenkins.token";

  /** Repository property for the jenkins ci server url */
  public static final String PROPERTY_JENKINS_URL = "jenkins.url";

  /** Repository property for the username for jenkins */
  public static final String PROPERTY_JENKINS_USERNAME = "jenkins.username";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new jenkins configuration object. This constructor reads the
   * properties and stores it in the configuration object.
   *
   *
   * @param repository the repository which triggered the hook
   */
  public JenkinsConfiguration(Repository repository)
  {
    this.url = repository.getProperty(PROPERTY_JENKINS_URL);
    this.project = repository.getProperty(PROPERTY_JENKINS_PROJECT);
    this.token = repository.getProperty(PROPERTY_JENKINS_TOKEN);
    this.username = repository.getProperty(PROPERTY_JENKINS_USERNAME);
    this.apiToken = repository.getProperty(PROPERTY_JENKINS_APITOKEN);
    
    Set<String> set = new HashSet<String>();
    String branchString = repository.getProperty(PROPERTY_JENKINS_BRANCHES);
    if (Util.isNotEmpty(branchString))
    {
      for (String branch : branchString.split(","))
      {
        set.add(branch.trim());
      }
    }
    this.branches = Collections.unmodifiableSet(set);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the api token which is used for authentication.
   * Note the authentication is only used if the username and
   * the api token are non null.
   *
   *
   * @return api token of the user
   */
  public String getApiToken()
  {
    return apiToken;
  }

  /**
   * Returns comma separated list of branches. The hook will only be executed, 
   * if the branch is listed. If the set is empty the hook will be executed on
   * every push.
   *
   *
   * @return comma separated list of branches
   *
   * @since 1.10
   */
  public Set<String> getBranches()
  {
    return branches;
  }

  /**
   * Returns the name of the jenkins project.
   *
   *
   * @return name of the jenkins project
   */
  public String getProject()
  {
    return project;
  }

  /**
   * Returns the jenkins authentication token.
   *
   *
   * @return jenkins authentication token
   */
  public String getToken()
  {
    return token;
  }

  /**
   * Return the url of the jenkins ci server.
   *
   *
   * @return url of the jenkins ci server
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Returns the username which is used for authentication.
   * Note the authentication is only used if the username and
   * the api token are non null.
   *
   *
   * @return username for authentication
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Return true, if the configuration is valid.
   *
   *
   * @return true, if the configuration is valid
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(url) && Util.isNotEmpty(project);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final String apiToken;

  /** Field description */
  private final Set<String> branches;

  /** Field description */
  private final String project;

  /** Field description */
  private final String token;

  /** Field description */
  private final String url;

  /** Field description */
  private final String username;
}
