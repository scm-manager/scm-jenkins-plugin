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


/**
 * The CsrfCrumb class holds every jenkins csrf protection information.
 * 
 * @author Sebastian Sdorra
 * @since 1.13
 */
public final class CsrfCrumb
{
  private final String crumb;
  private final String crumbRequestField;

  /**
   * Constructs a new {@link CsrfCrumb}.
   * 
   * @param crumb the crumb itself
   * @param crumbRequestField name of the request header
   */
  public CsrfCrumb(String crumb, String crumbRequestField)
  {
    this.crumb = crumb;
    this.crumbRequestField = crumbRequestField;
  }

  /**
   * Returns the crumb.
   * 
   * @return crumb
   */
  public String getCrumb()
  {
    return crumb;
  }

  /**
   * Returns the name of the http header.
   * 
   * @return http header name
   */
  public String getCrumbRequestField()
  {
    return crumbRequestField;
  }
  
}
