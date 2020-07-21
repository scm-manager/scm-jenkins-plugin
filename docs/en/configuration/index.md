---
title: Configuration
---
There are two quite different configurations for the SCM-Jenkins-plugin.

### Global configuration
In the global configuration you can set the Jenkins instance url and configure the VCS triggers. 
Also it is possible to disable the repository specific configuration.

![Jenkins Global Configuration](assets/global-config.png)

#### Repository Configuration
With the repository configuration the build jobs of the repository can be controlled. 
Just the Jenkins instance url and some kind of credentials are required. 
For authentication you can use an authentication token or an api token. 
You can filter the build job triggers by branches, so only build jobs of selected branches will be triggered on a repository push.

![Jenkins Repository Configuration](assets/repo-config.png)
