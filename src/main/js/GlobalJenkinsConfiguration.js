// @flow
import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import GlobalJenkinsConfigurationForm from "./GlobalJenkinsConfigurationForm";
import { translate } from "react-i18next";

type Props = {
  link: string,
  t: string => string
};

class GlobalJenkinsConfiguration extends React.Component<Props> {
  render(): React.ReactNode {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-jenkins-plugin.global.form.header")} />
        <Configuration
          link={link}
          t={t}
          render={props => <GlobalJenkinsConfigurationForm {...props} />}
        />
      </>
    );
  }
}

export default translate("plugins")(GlobalJenkinsConfiguration);
