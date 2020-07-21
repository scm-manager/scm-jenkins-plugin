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

If the Jenkins build job is parametrized you may also send parameters with each request.
The parameters consist of name and value pairs. The name must match the parameter name in the Jenkins build job exactly (case sensitiv).
The values may be absolute or relative values. Absolute values are for example `Trillian` as string parameter or `true` as boolean parameter.
The relative values can be used through an expression language from the repository push context (repository, commits).
It could be used like `${repository.id}` for the repository id or `${commit.author.mail}` to get the mail address from the committer.


Wenn es sich um einen parametrisierten Build Job im Jenkins handelt, können auch Build Parameter mit jedem Request geschickt werden. 
Die Build Parameter werden in einer zweispaltigen Tabelle gepflegt. 
In der linken Spalte wird der Name des Parameters angegeben, der exakt dem Namen im Jenkins Job entsprechen muss (Groß- und Kleinschreibung beachten).
Rechts werden die Werte der Parameter eingetragen, welche entweder absolut oder relativ sein können. 
Ein absoluter Parameter wäre beispielsweise `Trillian` als String Parameter oder `true` als boolscher Parameter. 
Relative Parameter beziehen sich auf den Repository Push bzw. die enthaltenen Commits.
Über eine Ausdruckssprache können Werte aus dem Kontext genutzt werden, z. B. `${repository.id}` für die Repository ID oder `${commit.author.mail}` für die E-Mail Adresse des Committers.

![Jenkins Repository Configuration](assets/repo-config.png)
