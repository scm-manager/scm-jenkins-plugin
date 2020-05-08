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
import {
  AddEntryToTableField,
  Checkbox,
  Configuration,
  InputField,
  LabelWithHelpIcon,
  RemoveEntryOfTableButton
} from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type LocalConfiguration = {
  apiToken: string;
  branches: string[];
  project: string;
  token: string;
  url: string;
  username: string;
  csrf: boolean;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;
  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
  t: (p: string) => string;
};

type State = LocalConfiguration & {
  configurationChanged: boolean;
};

class LocalJenkinsConfigurationForm extends React.Component<Props, State> {
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

  addBranchHandler = (newBranch: string) => {
    const branches = this.state.branches == null ? [] : this.state.branches;
    branches.push(newBranch);
    this.setState(
      {
        branches: branches
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

  deleteBranchHandler = (branchToDelete: string) => {
    const branches = this.state.branches == null ? [] : this.state.branches;
    const index = branches.indexOf(branchToDelete);
    if (index > -1) {
      branches.splice(index, 1);
    }
    this.setState({
      branches: branches
    });
    this.props.onConfigurationChange(
      {
        ...this.state
      },
      true
    );
  };

  render(): React.ReactNode {
    const { t, readOnly } = this.props;
    const branches = this.state.branches == null ? [] : this.state.branches;
    return (
      <>
        {this.renderConfigChangedNotification()}
        <InputField
          name={"url"}
          label={t("scm-jenkins-plugin.local.form.url")}
          helpText={t("scm-jenkins-plugin.local.form.urlHelp")}
          disabled={readOnly}
          value={this.state.url}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"project"}
          label={t("scm-jenkins-plugin.local.form.project")}
          helpText={t("scm-jenkins-plugin.local.form.projectHelp")}
          disabled={readOnly}
          value={this.state.project}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"token"}
          label={t("scm-jenkins-plugin.local.form.token")}
          helpText={t("scm-jenkins-plugin.local.form.tokenHelp")}
          disabled={readOnly}
          value={this.state.token}
          type="password"
          onChange={this.valueChangeHandler}
        />
        <Checkbox
          name={"csrf"}
          label={t("scm-jenkins-plugin.local.form.csrf")}
          helpText={t("scm-jenkins-plugin.local.form.csrfHelp")}
          checked={this.state.csrf}
          disabled={readOnly}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"username"}
          label={t("scm-jenkins-plugin.local.form.username")}
          helpText={t("scm-jenkins-plugin.local.form.usernameHelp")}
          disabled={readOnly}
          value={this.state.username}
          onChange={this.valueChangeHandler}
        />
        <InputField
          name={"apiToken"}
          label={t("scm-jenkins-plugin.local.form.apiToken")}
          helpText={t("scm-jenkins-plugin.local.form.apiTokenHelp")}
          disabled={readOnly}
          value={this.state.apiToken}
          type="password"
          onChange={this.valueChangeHandler}
        />

        <LabelWithHelpIcon
          label={t("scm-jenkins-plugin.local.form.branchesHeader")}
          helpText={t("scm-jenkins-plugin.local.form.branchesHelp")}
        />
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {branches.map(branch => {
              return (
                <tr key={branch}>
                  <td>{branch}</td>
                  <td>
                    <RemoveEntryOfTableButton
                      entryname={branch}
                      removeEntry={this.deleteBranchHandler}
                      label={t("scm-jenkins-plugin.local.form.branchesDelete")}
                      disabled={false}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>

        <AddEntryToTableField
          disabled={readOnly}
          fieldLabel={t("scm-jenkins-plugin.local.form.branches")}
          addEntry={this.addBranchHandler}
          buttonLabel={t("scm-jenkins-plugin.local.form.branchesAdd")}
        />
      </>
    );
  }

  renderConfigChangedNotification = () => {
    if (this.state.configurationChanged) {
      return (
        <div className="notification is-primary">
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

export default withTranslation("plugins")(LocalJenkinsConfigurationForm);
