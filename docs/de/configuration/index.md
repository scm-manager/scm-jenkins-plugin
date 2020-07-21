---
title: Konfiguration
---
Wie im SCM-Manager 2 üblich, gibt es eine globale und eine repository-spezifische Konfiguration für das Jenkins-Plugin.

### Globale Konfiguration
Über die globale Jenkins Konfiguration lassen sich die Jenkins Instanz-URL und die VCS Trigger konfigurieren. 
Außerdem kann dort die repository-spezifische Konfiguration deaktiviert werden.

![Jenkins Globale Konfiguration](assets/global-config.png)

#### Repository Konfiguration
Über die Repository Konfiguration des SCM-Jenkins-Plugin können die Builds des Repositories gesteuert werden. 
Dazu wird die Jenkins Instanz-URL und der Name des Projekts benötigt. 
Weiterhin kann man über unterschiedliche Zugangsdaten eine Verbindung mit einer gesicherten Jenkins Instanz herstellen. 
Es gibt die Möglichkeit einen Authentifizierungstoken oder einen API Token passend zum Benutzer für die Verbindung zu verwenden. 
Über die Branches lässt sich zusätzlich steuern, welche Repository Branches nach einem Push zum Bauen auf dem Jenkins getriggert werden sollen.

Wenn es sich um einen parametrisierten Build Job im Jenkins handelt, können auch Build Parameter mit jedem Request geschickt werden. 
Die Build Parameter werden in einer zweispaltigen Tabelle gepflegt. 
In der linken Spalte wird der Name des Parameters angegeben, der exakt dem Namen im Jenkins Job entsprechen muss (Groß- und Kleinschreibung beachten).
Rechts werden die Werte der Parameter eingetragen, welche entweder absolut oder relativ sein können. 
Ein absoluter Parameter wäre beispielsweise `Trillian` als String Parameter oder `true` als boolscher Parameter. 
Relative Parameter beziehen sich auf den Repository Push bzw. die enthaltenen Commits.
Über eine Ausdruckssprache können Werte aus dem Kontext genutzt werden, z. B. `${repository.id}` für die Repository ID oder `${commit.author.mail}` für die E-Mail Adresse des Committers.

![Jenkins Repository Konfiguration](assets/repo-config.png)
