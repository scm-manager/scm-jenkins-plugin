---
title: Zusammenspiel mit anderen Plugins
---

Die volle Funktionalität für die Anbindung an Jenkins bekommt der SCM-Manager, wenn neben diesem Plugin auch das
[CI-Plugin](https://scm-manager.org/plugins/scm-ci-plugin/) installiert wird. Zudem sollte im Jenkins das 
[SCM-Manager-Plugin](https://plugins.jenkins.io/scm-manager/) installiert sein. In diesem Fall können vom Jenkins
die Ergebnisse der Builds für einzelne Commits oder Pull Requests (mit dem
[Review-Plugin](https://scm-manager.org/plugins/scm-review-plugin/)) an den SCM-Manager übertragen und hier direkt
angezeigt und ausgewertet werden.

### Technischer User

Für den Zugriff vom Jenkins auf den SCM-Manager sollte im SCM-Manager ein technischer User eingerichtet werden. Dieser
benötigt entsprechende Berechtigungen. Soll dieser User nur für einzelne Repositories oder Namespaces berechtigt werden,
kann hierfür die Rolle "**CI-SERVER**" genutzt werden. Soll der User global berechtigt werden, so sind die folgenden
Berechtigungen notwendig:

- "Alle Repositories lesen"
- "CI-Status lesen"
- "CI-Status setzen"
- "Alle Pull Requests lesen" (nur mit installiertem [Review-Plugin](https://scm-manager.org/plugins/scm-review-plugin/))
