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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Notification, Subtitle, Title } from "@scm-manager/ui-core";
import { ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { HalRepresentation } from "@scm-manager/ui-types";

type GlobalConfiguration = HalRepresentation & {
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

type Props = {
  link: string;
};

export const GlobalJenkinsConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");

  return (
    <>
      <Title>{t("scm-jenkins-plugin.globalConfig.title")}</Title>
      <ConfigurationForm<GlobalConfiguration>
        link={link}
        translationPath={["plugins", "scm-jenkins-plugin.globalConfig"]}
      >
        <Subtitle>{t("scm-jenkins-plugin.globalConfig.generalSubtitle")}</Subtitle>
        <Notification type="warning">{t("scm-jenkins-plugin.globalConfig.warning")}</Notification>
        <Form.Row>
          <Form.Input name="url" autoFocus />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="disableRepositoryConfiguration" />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="disableGitTrigger" />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="disableMercurialTrigger" />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="disableSubversionTrigger" />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="disableEventTrigger" />
        </Form.Row>
        <Form.Row>
          <Form.Input name="username" />
        </Form.Row>
        <Form.Row>
          <Form.Input name="apiToken" type="password" />
        </Form.Row>
        <hr />
        <Subtitle>{t("scm-jenkins-plugin.globalConfig.gitOnlySubtitle")}</Subtitle>
        <Form.Row>
          <Form.Input name="gitAuthenticationToken" type="password" />
        </Form.Row>
      </ConfigurationForm>
    </>
  );
};
