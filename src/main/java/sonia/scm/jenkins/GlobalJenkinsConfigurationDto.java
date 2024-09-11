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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("java:S2160") // wo do not need equals and hashcode for dto
public class GlobalJenkinsConfigurationDto extends HalRepresentation {

  private boolean disableRepositoryConfiguration = false;
  private boolean disableSubversionTrigger = false;
  private boolean disableMercurialTrigger = false;
  private boolean disableGitTrigger = false;
  private boolean disableEventTrigger = false;
  private String url;
  private String username;
  private String apiToken;
  private String gitAuthenticationToken;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
