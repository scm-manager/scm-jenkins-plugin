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

import React from "react";
import { Checkbox, InputField, Configuration, Notification, Subtitle } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type GlobalConfiguration = {
  url: string;
  disableRepositoryConfiguration: boolean;
  disableMercurialTrigger: boolean;
  disableGitTrigger: boolean;
  disableEventTrigger: boolean;
  disableSubversionTrigger: boolean;
  username?: string;
  apiToken?: string;
  gitAuthenticationToken?: string;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

type State = GlobalConfiguration & {
  configurationChanged: boolean;
};

class GlobalJenkinsConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  valueChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  render(): React.ReactNode {
    const { t, readOnly } = this.props;
    return (
      <>
        <Subtitle subtitle={t("scm-jenkins-plugin.global.form.generalSubtitle")} />
        {this.renderConfigChangedNotification()}
        <Notification type={"warning"}>{t("scm-jenkins-plugin.global.form.warning")}</Notification>
        <InputField
          name={"url"}
          label={t("scm-jenkins-plugin.global.form.url")}
          helpText={t("scm-jenkins-plugin.global.form.urlHelp")}
          disabled={readOnly}
          value={this.state.url}
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"disableRepositoryConfiguration"}
          label={t("scm-jenkins-plugin.global.form.disableRepositoryConfiguration")}
          helpText={t("scm-jenkins-plugin.global.form.disableRepositoryConfigurationHelp")}
          checked={this.state.disableRepositoryConfiguration}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"disableGitTrigger"}
          label={t("scm-jenkins-plugin.global.form.disableGitTrigger")}
          helpText={t("scm-jenkins-plugin.global.form.disableGitTriggerHelp")}
          checked={this.state.disableGitTrigger}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"disableMercurialTrigger"}
          label={t("scm-jenkins-plugin.global.form.disableMercurialTrigger")}
          helpText={t("scm-jenkins-plugin.global.form.disableMercurialTriggerHelp")}
          checked={this.state.disableMercurialTrigger}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"disableSubversionTrigger"}
          label={t("scm-jenkins-plugin.global.form.disableSubversionTrigger")}
          helpText={t("scm-jenkins-plugin.global.form.disableSubversionTriggerHelp")}
          checked={this.state.disableSubversionTrigger}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"disableEventTrigger"}
          label={t("scm-jenkins-plugin.global.form.disableEventTrigger")}
          helpText={t("scm-jenkins-plugin.global.form.disableEventTriggerHelp")}
          checked={this.state.disableEventTrigger}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"username"}
          label={t("scm-jenkins-plugin.global.form.username")}
          helpText={t("scm-jenkins-plugin.global.form.usernameHelp")}
          disabled={readOnly}
          value={this.state.username}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"apiToken"}
          type="password"
          label={t("scm-jenkins-plugin.global.form.apiToken")}
          helpText={t("scm-jenkins-plugin.global.form.apiTokenHelp")}
          disabled={readOnly}
          value={this.state.apiToken}
          onChange={this.valueChangeHandler}
        />
        <hr />
        <Subtitle subtitle={t("scm-jenkins-plugin.global.form.gitOnlySubtitle")} />
        <InputField
          name={"gitAuthenticationToken"}
          type="password"
          label={t("scm-jenkins-plugin.global.form.gitAuthenticationToken")}
          helpText={t("scm-jenkins-plugin.global.form.gitAuthenticationTokenHelp")}
          disabled={readOnly}
          value={this.state.gitAuthenticationToken}
          onChange={this.valueChangeHandler}
        />
      </>
    );
  }

  renderConfigChangedNotification = () => {
    if (this.state.configurationChanged) {
      return (
        <div className="notification is-info">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                ...this.state,
                configurationChanged: false
              })
            }
          />
          {this.props.t("scm-jenkins-plugin.configurationChangedSuccess")}
        </div>
      );
    }
    return null;
  };
}

export default withTranslation("plugins")(GlobalJenkinsConfigurationForm);
