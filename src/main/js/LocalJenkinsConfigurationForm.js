//@flow

import React from "react";
import {
  AddEntryToTableField,
  Checkbox,
  Configuration,
  InputField,
  LabelWithHelpIcon,
  RemoveEntryOfTableButton
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type LocalConfiguration = {
  apiToken: string,
  branches: string[],
  project: string,
  token: string,
  url: string,
  username: string,
  csrf: boolean
};

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,
  onConfigurationChange: (Configuration, boolean) => void,
  t: string => string
};

type State = LocalConfiguration & {
  configurationChanged: boolean
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
      () => this.props.onConfigurationChange({ ...this.state }, true)
    );
  };

  addBranchHandler = (newBranch: string) => {
    let branches = this.state.branches == null ? [] : this.state.branches;
    branches.push(newBranch);
    this.setState(
      {
        branches: branches
      },
      () => this.props.onConfigurationChange({ ...this.state }, true)
    );
  };

  deleteBranchHandler = (branchToDelete: String) => {
    let branches = this.state.branches == null ? [] : this.state.branches;
    let index = branches.indexOf(branchToDelete);
    if (index > -1) {
      branches.splice(index, 1);
    }
    this.setState({
      branches: branches
    });
    this.props.onConfigurationChange({ ...this.state }, true);
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
              this.setState({ ...this.state, configurationChanged: false })
            }
          />
          {this.props.t("scm-jenkins-plugin.configurationChangedSuccess")}
        </div>
      );
    }
    return null;
  };
}

export default translate("plugins")(LocalJenkinsConfigurationForm);
