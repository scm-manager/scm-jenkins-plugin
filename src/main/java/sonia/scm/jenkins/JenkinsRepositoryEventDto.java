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

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.repository.api.ScmProtocol;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@SuppressWarnings("java:S2160")
class JenkinsRepositoryEventDto extends JenkinsEventDto {
  private String namespace;
  private String name;
  private String type;
  private String server;

  JenkinsRepositoryEventDto(EventTarget eventTarget, List<ScmProtocol> protocols) {
    super(new Links.Builder().array(protocols.stream().map(protocol -> Link.link(protocol.getType(), protocol.getUrl())).collect(Collectors.toList())).build(), eventTarget);
  }
}
