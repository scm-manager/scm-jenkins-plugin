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

import java.util.List;

@Getter
@Setter
@SuppressWarnings("java:S2160")
public class JenkinsEventDto extends HalRepresentation  {
  private String server;
  private List<AdditionalServerIdentification.Identification> identifications;
  private final EventTarget eventTarget;

  public JenkinsEventDto(EventTarget eventTarget) {
    this.eventTarget = eventTarget;
  }

  JenkinsEventDto(Links links, EventTarget eventTarget) {
    super(links);
    this.eventTarget = eventTarget;
  }
}
