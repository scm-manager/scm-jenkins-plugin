---
title: Testen mit Jenkins
---

Um eine laufende Jenkins-Instanz zu erhalten, können Sie Docker und die vorbereitete Jenkins-Umgebung verwenden:

```
mvn generate-resources
docker-compose up
```

Melden Sie sich bei Jenkins mit den Anmeldedaten scmadmin/scmadmin an. 
Klicken Sie dann oben rechts auf den Benutzer und wählen Sie im linken Menü "Konfigurieren". 
Dort können Sie ein neues API-Token erstellen, indem Sie auf "Neues Token hinzufügen" klicken. 
Tun Sie dies und kopieren Sie den generierten Hash. 
Dieser wird im Folgenden als "API-Token" bezeichnet.

Diese Jenkins-Instanz hat einen Job konfiguriert, der von `http://172.18.0.1:8081/scm/repo/scmadmin/git` pulled. 
Sie können dieses Repository in einer laufenden SCM-Manager-Instanz mit curl erstellen:

```
curl -u scmadmin:scmadmin --data '{
"contact": "zaphod.beeblebrox@hitchhiker.com",
"name": "git",
"archived": false,
"defaultBranch": "branch",
"type": "git"
}' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories/
```

#### Repository-bezogene Konfiguration

Stellen Sie Jenkins für dieses Repository mit curl wie folgt ein (bitte fügen Sie Ihren oben generierten API-Token ein):

```
curl -u scmadmin:scmadmin --data '{
"apiToken":"<API-Token>",
"branches":["main"],
"project":"scm",
"token":"SOME_TOKEN",
"url":"http://localhost:8082/",
"username":"scmadmin",
"csrf":false
}' --header "Content-Type: application/json" -X PUT http://localhost:8081/scm/api/v2/config/jenkins/scmadmin/git
```

Sie können nun das Repository klonen:

```
git clone http://scmadmin@localhost:8081/scm/repo/scmadmin/git
```

Wann immer Sie etwas im main-Branch ändern, sollte ein Build in Jenkins ausgelöst werden.

#### Globale Konfiguration

Deaktivieren Sie die repository-bezogene Konfiguration und erstellen Sie eine globale:

```
curl -u scmadmin:scmadmin --data '{
"disableRepositoryConfiguration":true,
"disableMercurialTrigger":false,
"disableGitTrigger":false,
"url":"http://localhost:8082/"
}' --header "Content-Type: application/json" -X PUT http://localhost:8081/scm/api/v2/config/jenkins
```

Wann immer Sie etwas im main-Branch ändern, sollte ein Build in Jenkins ausgelöst werden.
