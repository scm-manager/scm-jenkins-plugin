/**
 * Copyright (c) 2014, Sebastian Sdorra
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
