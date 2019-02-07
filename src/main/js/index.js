// @flow

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components"
import GlobalJenkinsConfiguration from "./GlobalJenkinsConfiguration";
import LocalJenkinsConfiguration from "./LocalJenkinsConfiguration";

cfgBinder.bindGlobal("/jenkins", "scm-jenkins-plugin.global.nav-link", "jenkinsConfig", GlobalJenkinsConfiguration);
cfgBinder.bindRepositorySetting("/jenkins", "scm-jenkins-plugin.local.nav-link", "jenkinsConfig", LocalJenkinsConfiguration);
