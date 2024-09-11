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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlEncryptionAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jenkins-config")
@Getter
@Setter
@NoArgsConstructor
public class GlobalJenkinsConfiguration implements Validateable {
  @XmlElement(name = "disable-repository-configuration")
  private boolean disableRepositoryConfiguration = false;

  @XmlElement(name = "disable-mercurial-trigger")
  private boolean disableMercurialTrigger = false;

  @XmlElement(name = "disable-git-trigger")
  private boolean disableGitTrigger = false;

  @XmlElement(name = "disable-subversion-trigger")
  private boolean disableSubversionTrigger = false;

  @XmlElement(name = "disable-event-trigger")
  private boolean disableEventTrigger = false;

  private String username;

  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String apiToken;

  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String gitAuthenticationToken;

  private String url;

  public GlobalJenkinsConfiguration(String url, boolean disableGitTrigger, boolean disableMercurialTrigger, boolean disableSubversionTrigger, boolean disableRepositoryConfiguration) {
    this.url = url;
    this.disableGitTrigger = disableGitTrigger;
    this.disableMercurialTrigger = disableMercurialTrigger;
    this.disableSubversionTrigger = disableSubversionTrigger;
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(url);
  }

}
