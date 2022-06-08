# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.2.4 - 2022-06-08
### Fixed
- Disable svn hook without jenkins url ([#48](https://github.com/scm-manager/scm-jenkins-plugin/pull/48))

## 2.2.3 - 2022-04-29
### Fixed
- Replace passwords/tokens with "dummy" only if they are not empty ([#43](https://github.com/scm-manager/scm-jenkins-plugin/pull/43))

## 2.2.2 - 2022-01-28
### Fixed
- Exception in migration from v1 with missing branches ([#37](https://github.com/scm-manager/scm-jenkins-plugin/pull/37))

## 2.2.1 - 2021-03-26
### Fixed
- Method names for open api spec ([#19](https://github.com/scm-manager/scm-jenkins-plugin/pull/19))

## 2.2.0 - 2020-11-20
### Added
- Send trigger events to the global jenkins instance to trigger updates for namespace navigator items ([#13](https://github.com/scm-manager/scm-jenkins-plugin/pull/13))
- Add SVN global hook handler ([#14](https://github.com/scm-manager/scm-jenkins-plugin/pull/14))

### Changed
- Set span kind for http requests (for Trace Monitor)

## 2.1.0 - 2020-08-05
### Added
- Documentation in German ([#4](https://github.com/scm-manager/scm-jenkins-plugin/pull/4))
- Add build job parameters to jenkins repository config ([#5](https://github.com/scm-manager/scm-jenkins-plugin/pull/5))
- Allow variable build parameters from context ([#7](https://github.com/scm-manager/scm-jenkins-plugin/pull/7) and [#10](https://github.com/scm-manager/scm-jenkins-plugin/pull/10))
- Triggers the web hook of the [Jenkins SCM-Manager plugin](https://github.com/jenkinsci/scm-manager-plugin) ([9](https://github.com/scm-manager/scm-jenkins-plugin/pull/9))

## 2.0.0 - 2020-06-04
### Changed
- Rebuild for api changes from core

## 2.0.0-rc3 - 2020-05-08
### Changed
- Changeover to MIT license ([#2](https://github.com/scm-manager/scm-jenkins-plugin/pull/2))

### Fixed
- Encrypt api token on jenkins configuration ([#3](https://github.com/scm-manager/scm-jenkins-plugin/pull/3))

## 2.0.0-rc2 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin. ([#1](https://github.com/scm-manager/scm-jenkins-plugin/pull/1))

