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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link CsrfCrumbParser}.
 * @author Sebastian Sdorra
 */
public class CsrfCrumbParserTest {

  private static final String RESPONSE = "<defaultCrumbIssuer _class='sp'><crumb>abc</crumb><crumbRequestField>Jenkins-Crumb</crumbRequestField></defaultCrumbIssuer>";

  /**
   * Tests {@link CsrfCrumbParser#parse(java.io.InputStream)}.
   * 
   * @throws IOException 
   */
  @Test
  public void testParse() throws IOException
  {
    CsrfCrumb crumb = CsrfCrumbParser.parse(createInputStreamFromString(RESPONSE));
    assertEquals("abc", crumb.getCrumb());
    assertEquals("Jenkins-Crumb", crumb.getCrumbRequestField());
  }
  
  private InputStream createInputStreamFromString(String content) throws IOException
  {
    return new ByteArrayInputStream(content.getBytes("UTF-8"));
  }

}