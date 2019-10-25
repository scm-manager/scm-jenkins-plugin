import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import LocalJenkinsConfigurationForm from "./LocalJenkinsConfigurationForm";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation & {
  link: string;
};

class LocalJenkinsConfiguration extends React.Component<Props> {
  render(): React.ReactNode {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-jenkins-plugin.local.form.header")} />
        <Configuration link={link} t={t} render={props => <LocalJenkinsConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(LocalJenkinsConfiguration);
