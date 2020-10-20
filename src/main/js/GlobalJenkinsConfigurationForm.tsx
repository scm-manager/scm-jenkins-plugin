/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { Checkbox, Configuration, InputField } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type GlobalConfiguration = {
  url: string;
  disableRepositoryConfiguration: boolean;
  disableMercurialTrigger: boolean;
  disableGitTrigger: boolean;
  disableEventTrigger: boolean;
  disableSubversionTrigger: boolean;
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
        {this.renderConfigChangedNotification()}
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
