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

//~--- non-JDK imports --------------------------------------------------------

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
@NoArgsConstructor
public class GlobalJenkinsConfiguration implements Validateable
{

  public GlobalJenkinsConfiguration(String url, boolean disableGitTrigger, boolean disableMercurialTrigger, boolean disableRepositoryConfiguration) {
    this.url = url;
    this.disableGitTrigger = disableGitTrigger;
    this.disableMercurialTrigger = disableMercurialTrigger;
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }

  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(url);
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
