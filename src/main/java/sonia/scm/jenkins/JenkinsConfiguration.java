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

import lombok.EqualsAndHashCode;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlEncryptionAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
@EqualsAndHashCode
public class JenkinsConfiguration implements Validateable {

  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String apiToken;
  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String token;
  private Set<String> branches = new HashSet<>();
  private String project;
  private String url;
  private String username;
  private Set<BuildParameter> buildParameters;

  /**
   * Returns the api token which is used for authentication.
   * Note the authentication is only used if the username and
   * the api token are non null.
   *
   * @return api token of the user
   */
  public String getApiToken() {
    return apiToken;
  }

  public String getToken() { return token; }

  /**
   * Returns comma separated list of branches. The hook will only be executed,
   * if the branch is listed. If the set is empty the hook will be executed on
   * every push.
   *
   * @return comma separated list of branches
   * @since 1.10
   */
  public Set<String> getBranches() {
    return branches;
  }

  /**
   * Returns the name of the jenkins project.
   *
   * @return name of the jenkins project
   */
  public String getProject() {
    return project;
  }

  /**
   * Return the url of the jenkins ci server.
   *
   * @return url of the jenkins ci server
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns the username which is used for authentication.
   * Note the authentication is only used if the username and
   * the api token are non null.
   *
   * @return username for authentication
   */
  public String getUsername() {
    return username;
  }


  public Set<BuildParameter> getBuildParameters() {
    if (buildParameters == null) {
      return new HashSet<>();
    }
    return buildParameters;
  }

  public void setApiToken(String apiToken) {
    this.apiToken = apiToken;
  }

  public void setToken(String token) { this.token = token; }

  public void setBranches(Set<String> branches) {
    this.branches = branches;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setBuildParameters(Set<BuildParameter> buildParameters) {
    this.buildParameters = buildParameters;
  }

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(url) && Util.isNotEmpty(project);
  }
}
