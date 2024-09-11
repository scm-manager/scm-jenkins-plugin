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
import {
  AddEntryToTableField,
  AddKeyValueEntryToTableField,
  Checkbox,
  Column,
  Configuration,
  InputField,
  LabelWithHelpIcon,
  RemoveEntryOfTableButton,
  Table
} from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type BuildParameter = {
  name: string;
  value: string;
};

type LocalConfiguration = {
  apiToken: string;
  branches: string[];
  project: string;
  token: string;
  url: string;
  username: string;
  csrf: boolean;
  buildParameters: BuildParameter[];
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

  addBuildParameterHandler = (name: string, value: string) => {
    let params: BuildParameter[] = [];

    if (this.state.buildParameters?.length > 0) {
      params = [...this.state.buildParameters];
    }
    params.push({ name, value });

    this.setState(
      {
        buildParameters: params
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

  removeBuildParameter = (parameter: BuildParameter) => {
    const params = this.state.buildParameters;
    const index = params.indexOf(parameter);
    if (index !== -1) {
      params.splice(index, 1);
      this.setState({ buildParameters: params }, () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
      );
    }
  };

  render() {
    const { t, readOnly } = this.props;
    const branches = this.state.branches == null ? [] : this.state.branches;
    const buildParams = !this.state.buildParameters ? [] : this.state.buildParameters;
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
          errorMessage=""
        />
        <LabelWithHelpIcon
          label={t("scm-jenkins-plugin.local.form.parameterLabel")}
          helpText={t("scm-jenkins-plugin.local.form.parameterHelpText")}
        />
        {buildParams.length > 0 && (
          <Table data={buildParams}>
            <Column header={t("scm-jenkins-plugin.local.form.parameterName")}>
              {(row: BuildParameter) => <p className="is-word-break">{row.name}</p>}
            </Column>
            <Column header={t("scm-jenkins-plugin.local.form.parameterValue")}>
              {(row: BuildParameter) => <p className="is-word-break">{row.value}</p>}
            </Column>
            <Column header={""}>
              {(row: BuildParameter) => (
                <RemoveEntryOfTableButton
                  entryname={row.name}
                  removeEntry={() => this.removeBuildParameter(row)}
                  label={t("scm-jenkins-plugin.local.form.deleteParameter")}
                  disabled={false}
                />
              )}
            </Column>
          </Table>
        )}
        <AddKeyValueEntryToTableField
          keyFieldLabel={t("scm-jenkins-plugin.local.form.parameterName")}
          keyHelpText={t("scm-jenkins-plugin.local.form.parameterNameHelp")}
          valueFieldLabel={t("scm-jenkins-plugin.local.form.parameterValue")}
          valueHelpText={t("scm-jenkins-plugin.local.form.parameterValueHelp")}
          addEntry={(name, value) => this.addBuildParameterHandler(name, value)}
          buttonLabel={t("scm-jenkins-plugin.local.form.addParameter")}
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
