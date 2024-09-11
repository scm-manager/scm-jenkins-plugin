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