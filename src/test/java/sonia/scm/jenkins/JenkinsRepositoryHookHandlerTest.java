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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.net.ahc.AdvancedHttpClient;

import javax.inject.Provider;

import static org.junit.Assert.*;


/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class JenkinsRepositoryHookHandlerTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testEscape()
  {
    assertEquals("https://ci.scm-manager.org",
      handler.escape("https://ci.scm-manager.org"));
    assertEquals("https://ci.scm-manager.org/path",
      handler.escape("https://ci.scm-manager.org/path"));
    assertEquals("https://ci.scm-manager.org/some/deep/path",
      handler.escape("https://ci.scm-manager.org/some/deep/path"));
    assertEquals("https://ci.scm-manager.org/path?with=query&param=true",
      handler.escape("https://ci.scm-manager.org/path?with=query&param=true"));
    assertEquals("https://ci.scm-manager.org/with%20spaces",
      handler.escape("https://ci.scm-manager.org/with spaces"));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    handler = new JenkinsRepositoryHookHandler(httpClientProvider,
      configuration);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private JenkinsConfiguration configuration;

  /** Field description */
  private JenkinsRepositoryHookHandler handler;

  /** Field description */
  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;
}
