/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.jenkins;

import lombok.EqualsAndHashCode;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlEncryptionAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
  private Set<String> branches = new HashSet<>();
  private String project;
  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String token;
  private String url;
  private String username;
  private boolean csrf;
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
   * Returns the jenkins authentication token.
   *
   * @return jenkins authentication token
   */
  public String getToken() {
    return token;
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

  /**
   * Returns {@code true} if the jenkins instance is csrf protected.
   *
   * @return {@code true} if jenkins is csrf protected
   * @since 1.12
   */
  public boolean isCsrf() {
    return csrf;
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

  public void setBuildParameters(Set<BuildParameter> buildParameters) {
    this.buildParameters = buildParameters;
  }

  /**
   * Return true, if the configuration is valid.
   *
   * @return true, if the configuration is valid
   */
  @Override
  public boolean isValid() {
    return Util.isNotEmpty(url) && Util.isNotEmpty(project);
  }
}
