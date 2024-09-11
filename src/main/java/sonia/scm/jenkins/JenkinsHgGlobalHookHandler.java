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

import com.google.inject.Provider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.api.RepositoryServiceFactory;

class JenkinsHgGlobalHookHandler extends JenkinsGlobalHookHandler {

  public static final String TYPE_MERCURIAL = "hg";
  private static final String URL_MERCURIAL = "/mercurial/notifyCommit";

  private final GlobalJenkinsConfiguration configuration;

  JenkinsHgGlobalHookHandler(Provider<AdvancedHttpClient> httpClientProvider, GlobalJenkinsConfiguration configuration, RepositoryServiceFactory repositoryServiceFactory) {
    super(httpClientProvider, configuration, repositoryServiceFactory);
    this.configuration = configuration;
  }

  String createUrlSuffix() {
    if (!configuration.isDisableMercurialTrigger()) {
      return URL_MERCURIAL;
    }
    return null;
  }
}
