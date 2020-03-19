/**
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

import sonia.scm.Validateable;
import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for Jenkins Hook. This configuration reads the properties from
 * the repository. The properties can be configured from web interface.
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jenkins-config")
public class JenkinsConfiguration implements Validateable
{

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
   * Returns {@code true} if the jenkins instance is csrf protected.
   *
   * @since 1.12
   * @return {@code true} if jenkins is csrf protected
   */
  public boolean isCsrf()
  {
    return csrf;
  }

  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
  }

  public void setBranches(Set<String> branches) {
    this.branches = branches;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setCsrf(boolean csrf) {
    this.csrf = csrf;
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
  private String apiToken;

  /** Field description */
  private Set<String> branches = new HashSet<>();

  /** Field description */
  private String project;

  /** Field description */
  private String token;

  /** Field description */
  private String url;

  /** Field description */
  private String username;
  
  /** Field description */
  private boolean csrf;
}
