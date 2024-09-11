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

import com.cloudogu.scm.ssh.ConfigStore;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;

import jakarta.inject.Inject;

@Extension
@Requires("scm-ssh-plugin")
public class SshAdditionalServerIdentification implements AdditionalServerIdentification {

  private final ConfigStore sshConfigStore;

  @Inject
  public SshAdditionalServerIdentification(ConfigStore sshConfigStore) {
    this.sshConfigStore = sshConfigStore;
  }

  @Override
  public Identification get() {
    return new Identification("ssh", sshConfigStore.getBaseUrl() + ":" + sshConfigStore.getPort());
  }
}
