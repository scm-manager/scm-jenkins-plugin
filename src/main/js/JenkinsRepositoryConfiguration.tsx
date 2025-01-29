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

import React, { FC, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Level, Subtitle } from "@scm-manager/ui-core";
import { ChipInputField, ConfigurationForm, Form } from "@scm-manager/ui-forms";
import { HalRepresentation } from "@scm-manager/ui-types";

type BuildParameter = {
  name: string;
  value: string;
};

type RepositoryConfiguration = HalRepresentation & {
  apiToken: string;
  branches: string[];
  project: string;
  token: string;
  url: string;
  username: string;
  csrf: boolean;
  buildParameters: BuildParameter[];
};

type Props = {
  link: string;
};

export const JenkinsRepositoryConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const branchesRef = useRef<HTMLInputElement>(null);

  return (
    <>
      <Subtitle>{t("scm-jenkins-plugin.repoConfig.subtitle")}</Subtitle>
      <ConfigurationForm<RepositoryConfiguration>
        link={link}
        translationPath={["plugins", "scm-jenkins-plugin.repoConfig"]}
      >
        <Form.Row>
          <Form.Input name="url" autoFocus />
        </Form.Row>
        <Form.Row>
          <Form.Input name="project" />
        </Form.Row>
        <Form.Row>
          <Form.Input name="token" type="password" />
        </Form.Row>
        <Form.Row>
          <Form.Checkbox name="csrf" />
        </Form.Row>
        <Form.Row>
          <Form.Input name="username" />
        </Form.Row>
        <Form.Row>
          <Form.Input name="apiToken" type="password" />
        </Form.Row>
        <Form.Row>
          <Form.ChipInput name="branches" ref={branchesRef} />
        </Form.Row>
        <Level
          right={
            <ChipInputField.AddButton inputRef={branchesRef}>
              {t("scm-jenkins-plugin.repoConfig.branches.add")}
            </ChipInputField.AddButton>
          }
        ></Level>
        <Form.ListContext name="buildParameters">
          <h3 className="subtitle is-5">{t("scm-jenkins-plugin.repoConfig.buildParameters.entity")}</h3>
          <Form.Table withDelete>
            <Form.Table.Column name="name" />
            <Form.Table.Column name="value" />
          </Form.Table>
          <Form.AddListEntryForm defaultValues={{ name: "", value: "" }}>
            <p className="mb-3">{t("scm-jenkins-plugin.repoConfig.buildParameters.helpText")}</p>
            <Form.Row>
              <Form.Input name="name" />
            </Form.Row>
            <Form.Row>
              <Form.Input name="value" />
            </Form.Row>
          </Form.AddListEntryForm>
        </Form.ListContext>
      </ConfigurationForm>
    </>
  );
};
