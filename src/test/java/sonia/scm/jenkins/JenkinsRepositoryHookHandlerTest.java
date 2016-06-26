/**
 * Copyright (c) 2015, Sebastian Sdorra
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
 * https://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.jenkins;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.net.HttpClient;

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
  private Provider<HttpClient> httpClientProvider;
}
