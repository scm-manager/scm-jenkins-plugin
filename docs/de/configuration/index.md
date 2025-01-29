---
title: Konfiguration
---
Wie im SCM-Manager 2 üblich, gibt es eine globale und eine repository-spezifische Konfiguration für das Jenkins-Plugin.

### Globale Konfiguration
Über die globale Jenkins Konfiguration lassen sich die Jenkins Instanz-URL und die VCS Trigger konfigurieren. 
Außerdem kann die repository-spezifische Konfiguration durch Owner von Repositories verbietet werden.

Der Git-Trigger erfordert Version 1.1.14 oder höher des Jenkins-Git-Plugins.
Der Mercurial-Trigger erfordert Version 1.38 oder höher des Jenkins-Mercurial-Plugins.

Der EventTrigger erfordert die Installation des [Jenkins SCM-Manager Plugins](https://plugins.jenkins.io/scm-manager/) in Jenkins.
Damit wird der Jenkins über Änderungen an Branches, Tags oder Pull Requests informiert.

Abhängig von der Jenkins-Konfiguration muss zusätzlich ein Benutzername mit einem gültigen API-Token gesetzt werden.
Dieser ist nur für SVN-Commit-Trigger-Anfragen erforderlich, wenn der CSRF-Schutz auf dem Jenkins CI-Server aktiviert ist.

Zusätzlich wird für Git Repositories ein sogenannter "notifyCommit 
access token" benötigt. Ein solcher kann in der globalen Sicherheitskonfiguration in Jenkins erstellt werden.

Schließlich kann die repository-spezifische Konfiguration sowie das Senden der Event-Trigger für bestimmte Repository
Typen deaktiviert werden.

![Jenkins Globale Konfiguration](assets/global-config.png)

#### Repository Konfiguration
Über die Repository Konfiguration des SCM-Jenkins-Plugin können die Builds einzelner Repositories konfiguriert werden.
Dazu wird die Jenkins Instanz-URL und der Name des Projekts benötigt. 
Weiterhin kann man über unterschiedliche Zugangsdaten eine Verbindung mit einer gesicherten Jenkins Instanz herstellen.
Es gibt die Möglichkeit einen Authentifizierungstoken (z. B. aus der globalen Sicherheitskonfiguration) und/oder einem
API-Token passend zum Benutzer für die Verbindung zu verwenden. 

Über die Branches lässt sich zusätzlich steuern, welche Repository Branches nach einem Push zum Bauen auf dem Jenkins getriggert werden sollen.

Wenn es sich um einen parametrisierten Build Job im Jenkins handelt, können auch Build Parameter mit jedem Request geschickt werden. 
Die Build Parameter werden in einer zweispaltigen Tabelle gepflegt. 
In der linken Spalte wird der Name des Parameters angegeben, der exakt dem Namen im Jenkins Job entsprechen muss (Groß- und Kleinschreibung beachten).
Rechts werden die Werte der Parameter eingetragen, welche entweder absolut oder relativ sein können. 
Ein absoluter Parameter wäre beispielsweise `Trillian` als String Parameter oder `true` als boolescher Parameter. 
Relative Parameter beziehen sich auf den Repository Push bzw. die enthaltenen Commits.
Über eine Ausdruckssprache können Werte aus dem Kontext genutzt werden, z. B. `${repository.id}` für die Repository ID oder `${commit.author.mail}` für die E-Mail-Adresse des Committers.

![Jenkins Repository Konfiguration](assets/repo-config.png)
