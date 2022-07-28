---
title: Interaction with other Plugins
---

The whole benefit for the interaction with Jenkins is achieved with the addition of the
[CI plugin](https://scm-manager.org/plugins/scm-ci-plugin/) in SCM-Manager. Furthermore, the
[SCM-Manager plugin](https://plugins.jenkins.io/scm-manager/) should be installed in Jenkins. In this case, results
from builds for single commits or pull requests (from the
[review plugin](https://scm-manager.org/plugins/scm-review-plugin/)) can be sent to SCM-Manager and will be directly
available for further evaluation.

### Technical User

For the access from Jenkins to SCM-Manager, a technical user should be created. This user needs appropriate permissions.
If the access should be granted for only single repositories or namespaces only, the permission role "**CI-SERVER**"
can be used. To grant access for all repositories, the following permissions have to be set:

- "Read all repositories"
- "Read CI status"
- "Modify and set CI status"
- "Read all pull requests" (only if the [review plugin](https://scm-manager.org/plugins/scm-review-plugin/) is installed)
