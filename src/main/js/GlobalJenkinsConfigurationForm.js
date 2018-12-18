//@flow

import React from "react";
import {Checkbox, Configuration, InputField} from "@scm-manager/ui-components";
import {translate} from "react-i18next";

type GlobalConfiguration = {
  url: string,
  disableRepositoryConfiguration: boolean,
  disableMercurialTrigger: boolean,
  disableGitTrigger: boolean
}

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,
  onConfigurationChange: (Configuration, boolean) => void,
  t: (string) => string
}

type State = GlobalConfiguration

class GlobalJenkinsConfigurationForm extends React.Component<Props, State> {

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

  render(): React.ReactNode {
    const {t, readOnly} = this.props;
    return (
      <>
        <InputField name={"url"}
                    label={t("scm-jenkins-plugin.global.form.url")}
                    helpText={t("scm-jenkins-plugin.global.form.urlHelp")}
                    disabled={readOnly}
                    value={this.state.url}
                    onChange={this.valueChangeHandler}/>
        <Checkbox name={"disableRepositoryConfiguration"}
                  label={t("scm-jenkins-plugin.global.form.disableRepositoryConfiguration")}
                  helpText={t("scm-jenkins-plugin.global.form.disableRepositoryConfigurationHelp")}
                  checked={this.state.disableRepositoryConfiguration}
                  disabled={readOnly}
                  onChange={this.valueChangeHandler}/>
        <Checkbox name={"disableGitTrigger"}
                  label={t("scm-jenkins-plugin.global.form.disableGitTrigger")}
                  helpText={t("scm-jenkins-plugin.global.form.disableGitTriggerHelp")}
                  checked={this.state.disableGitTrigger}
                  disabled={readOnly}
                  onChange={this.valueChangeHandler}/>
        <Checkbox name={"disableMercurialTrigger"}
                  label={t("scm-jenkins-plugin.global.form.disableMercurialTrigger")}
                  helpText={t("scm-jenkins-plugin.global.form.disableMercurialTriggerHelp")}
                  checked={this.state.disableMercurialTrigger}
                  disabled={readOnly}
                  onChange={this.valueChangeHandler}/>
      </>
    );
  }
}

export default translate("plugins")(GlobalJenkinsConfigurationForm);
