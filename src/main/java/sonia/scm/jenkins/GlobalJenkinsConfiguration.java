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
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jenkins-config")
public class GlobalJenkinsConfiguration implements Validateable
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDisableGitTrigger()
  {
    return disableGitTrigger;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDisableMercurialTrigger()
  {
    return disableMercurialTrigger;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isDisableRepositoryConfiguration()
  {
    return disableRepositoryConfiguration;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(url);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param disableGitTrigger
   */
  public void setDisableGitTrigger(boolean disableGitTrigger)
  {
    this.disableGitTrigger = disableGitTrigger;
  }

  /**
   * Method description
   *
   *
   * @param disableMercurialTrigger
   */
  public void setDisableMercurialTrigger(boolean disableMercurialTrigger)
  {
    this.disableMercurialTrigger = disableMercurialTrigger;
  }

  /**
   * Method description
   *
   *
   * @param disableRepositoryConfiguration
   */
  public void setDisableRepositoryConfiguration(boolean disableRepositoryConfiguration)
  {
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "disable-repository-configuration")
  private boolean disableRepositoryConfiguration = false;

  /** Field description */
  @XmlElement(name = "disable-mercurial-trigger")
  private boolean disableMercurialTrigger = false;

  /** Field description */
  @XmlElement(name = "disable-git-trigger")
  private boolean disableGitTrigger = false;

  /** Field description */
  private String url;
}
