//@flow

import React from "react";
import {
  AddEntryToTableField,
  Checkbox,
  Configuration,
  DeleteButton,
  InputField, LabelWithHelpIcon
} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type LocalConfiguration = {
  apiToken: string,
  branches: string[],
  project: string,
  token: string,
  url: string,
  username: string,
  csrf: boolean
}

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,
  onConfigurationChange: (Configuration, boolean) => void,
  t: (string) => string
}

type State = LocalConfiguration

class LocalJenkinsConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  valueChangeHandler = (value: string, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onConfigurationChange({...this.state}, true));
  };

  addBranchHandler = (newBranch: string) => {
    console.log("ADD BRANCH");
    let branches = this.state.branches == null? []: this.state.branches;
    console.log(branches);
    branches.push(newBranch);
    console.log(branches);
    this.setState({
      "branches": branches
    }, () => this.props.onConfigurationChange({...this.state}, true));
  };

  deleteBranchHandler = (branchToDelete: any) => {
    console.log(branchToDelete);
    let branches = this.state.branches == null? []: this.state.branches;
    console.log(branches);
    branches.pop(branchToDelete);
    console.log(branches);
    this.setState({
      "branches": branches
    });
    this.props.onConfigurationChange({...this.state}, true);
  };

  render(): React.ReactNode {
    const {t, readOnly} = this.props;
    const branches = this.state.branches == null ? [] : this.state.branches;
    return (
      <>
        <InputField name={"url"}
                    label={t("scm-jenkins-plugin.local.form.url")}
                    helpText={t("scm-jenkins-plugin.local.form.urlHelp")}
                    disabled={readOnly}
                    value={this.state.url}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"project"}
                    label={t("scm-jenkins-plugin.local.form.project")}
                    helpText={t("scm-jenkins-plugin.local.form.projectHelp")}
                    disabled={readOnly}
                    value={this.state.project}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"token"}
                    label={t("scm-jenkins-plugin.local.form.token")}
                    helpText={t("scm-jenkins-plugin.local.form.tokenHelp")}
                    disabled={readOnly}
                    value={this.state.token}
                    onChange={this.valueChangeHandler}/>
        <Checkbox name={"csrf"}
                  label={t("scm-jenkins-plugin.local.form.csrf")}
                  helpText={t("scm-jenkins-plugin.local.form.csrfHelp")}
                  checked={this.state.csrf}
                  disabled={readOnly}
                  onChange={this.valueChangeHandler}/>
        <InputField name={"username"}
                    label={t("scm-jenkins-plugin.local.form.username")}
                    helpText={t("scm-jenkins-plugin.local.form.usernameHelp")}
                    disabled={readOnly}
                    value={this.state.username}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"apiToken"}
                    label={t("scm-jenkins-plugin.local.form.apiToken")}
                    helpText={t("scm-jenkins-plugin.local.form.apiTokenHelp")}
                    disabled={readOnly}
                    value={this.state.apiToken}
                    onChange={this.valueChangeHandler}/>


        <LabelWithHelpIcon
          label={t("scm-jenkins-plugin.local.form.branchesHeader")}
          helpText={t("scm-jenkins-plugin.local.form.branchesHelp")}
        />
        <table className="table is-hoverable is-fullwidth">
          <tbody>
          {
            branches.map(branch => {
            return (
              <tr>
                <td>{branch}</td>
                <td>
                  <DeleteButton
                    label={t("scm-jenkins-plugin.local.form.branchesDelete")}
                    action={this.deleteBranchHandler}
                  />
                </td>
              </tr>
            );
          })}
          </tbody>
        </table>

        <AddEntryToTableField disabled={readOnly}
                              fieldLabel={t("scm-jenkins-plugin.local.form.branches")}
                              addEntry={this.addBranchHandler}
                              buttonLabel={t("scm-jenkins-plugin.local.form.branchesAdd")}/>
      </>
    );
  }
}

export default translate("plugins")(LocalJenkinsConfigurationForm);
