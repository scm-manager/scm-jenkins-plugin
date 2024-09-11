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

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

class ProtocolResolver {

  private final RepositoryServiceFactory repositoryServiceFactory;

  @Inject
  ProtocolResolver(RepositoryServiceFactory repositoryServiceFactory) {
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  List<ScmProtocol> getProtocols(Repository repository) {
    try (final RepositoryService repositoryService = repositoryServiceFactory.create(repository)) {
      return repositoryService.getSupportedProtocols().collect(Collectors.toList());
    }
  }
}
