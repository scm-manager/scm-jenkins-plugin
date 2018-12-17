//@flow

import React from "react";
import {
  AddEntryToTableField,
  Checkbox,
  Configuration,
  DeleteButton,
  InputField
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
        <InputField name={"apiToken"}
                    label={t("scm-jenkins-plugin.local.form.apiToken")}
                    disabled={readOnly}
                    value={this.state.apiToken}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"project"}
                    label={t("scm-jenkins-plugin.local.form.project")}
                    disabled={readOnly}
                    value={this.state.project}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"token"}
                    label={t("scm-jenkins-plugin.local.form.token")}
                    disabled={readOnly}
                    value={this.state.token}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"url"}
                    label={t("scm-jenkins-plugin.local.form.url")}
                    disabled={readOnly}
                    value={this.state.url}
                    onChange={this.valueChangeHandler}/>
        <InputField name={"username"}
                    label={t("scm-jenkins-plugin.local.form.username")}
                    disabled={readOnly}
                    value={this.state.username}
                    onChange={this.valueChangeHandler}/>
        <Checkbox name={"csrf"}
                  label={t("scm-jenkins-plugin.local.form.csrf")}
                  checked={this.state.csrf}
                  disabled={readOnly}
                  onChange={this.valueChangeHandler}/>


        <table className="table is-hoverable is-fullwidth">
          <thead>
          <tr>
            <th>{t("scm-jenkins-plugin.local.form.branchesHeader")}</th>
            <th />
          </tr>
          </thead>
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
