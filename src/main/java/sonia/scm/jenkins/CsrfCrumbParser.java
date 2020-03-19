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

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sonia.scm.util.Util;

/**
 * Parses the content of a jenkins http response to get a csrf crumb.
 *
 * @author Sebastian Sdorra
 * @since 1.13
 */
public class CsrfCrumbParser
{
  
  /**
   * the logger for CsrfCrumbParser
   */
  private static final Logger logger = LoggerFactory.getLogger(CsrfCrumbParser.class);

  private CsrfCrumbParser()
  {
  }
  
  /**
   * Parses the input stream and returns a {@link CsrfCrumb} object or {@code null}.
   * 
   * @param content input stream
   * 
   * @return {@link CsrfCrumb} object or {@code null}
   * @throws IOException 
   */
  public static CsrfCrumb parse(InputStream content) throws IOException
  {
    Document doc = parseToDocument(content);

    String crumb = getContent(doc, "crumb");
    String field = getContent(doc, "crumbRequestField");
    
    CsrfCrumb csrfCrumb = null;
    if ( Util.isNotEmpty(crumb) && Util.isNotEmpty(field) )
    {
      csrfCrumb = new CsrfCrumb(crumb, field);
    } 
    else 
    {
      logger.warn("failed to extract csrf crumb fields");
    }
    
    return csrfCrumb;
  }
  
  private static String getContent(Document doc, String name)
  {
    String result = null;
    NodeList list = doc.getElementsByTagName(name);
    for (int i = 0; i<list.getLength(); i++ )
    {
      Node node = list.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE)
      {
        result = node.getTextContent();
        break;
      }
    }
    return result;
  }
  
  private static Document parseToDocument(InputStream content) throws IOException
  {
    try 
    {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      return docBuilder.parse(content);
    } 
    catch (ParserConfigurationException ex)
    {
      throw new IOException("failed to parse document", ex);
    } 
    catch (SAXException ex)
    {
      throw new IOException("failed to parse document", ex);
    }
  }
}
