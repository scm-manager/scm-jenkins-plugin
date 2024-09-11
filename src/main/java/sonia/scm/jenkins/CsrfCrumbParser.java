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
