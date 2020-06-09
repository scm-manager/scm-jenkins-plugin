### Testing with Jenkins

To get a running Jenkins instance you can use docker and the prepared Jenkins environment:

```
mvn generate-resources
docker-compose up
```

Log into Jenkins using credentials scmadmin/scmadmin. Then click the user on the top right and choose "Configure" in the left menu. There you can create a new
API Token clicking "Add new Token". Do so and copy the generated hash. This will be referenced in the following as "API Token".

This Jenkins instance has a job configured to poll from `http://172.18.0.1:8081/scm/repo/scmadmin/git`. You can create this repository in a running instance of SCM-Manager using curl:

```
curl -u scmadmin:scmadmin --data '{
  "contact": "zaphod.beeblebrox@hitchhiker.com",
  "name": "git",
  "archived": false,
  "defaultBranch": "branch",
  "type": "git"
}' --header "Content-Type: application/vnd.scmm-repository+json" http://localhost:8081/scm/api/v2/repositories/
```

#### Repository related configuration

Set the Jenkins for this repository using curl as follows (please insert your API Token you generated above):

```
curl -u scmadmin:scmadmin --data '{
    "apiToken":"<API Token>",
    "branches":["master"],
    "project":"scm",
    "token":"SOME_TOKEN",
    "url":"http://localhost:8082/",
    "username":"scmadmin",
    "csrf":false
    }' --header "Content-Type: application/json" -X PUT http://localhost:8081/scm/api/v2/config/jenkins/scmadmin/git
```

You can now clone the repository:

```
git clone http://scmadmin@localhost:8081/scm/repo/scmadmin/git
```

Whenever you change something on the master branch, a build should be triggered in Jenkins.

#### Global configuration

Disable the repository related configuration and create a global one:

```
curl -u scmadmin:scmadmin --data '{
    "disableRepositoryConfiguration":true,
    "disableMercurialTrigger":false,
    "disableGitTrigger":false,
    "url":"http://localhost:8082/"
    }' --header "Content-Type: application/json" -X PUT http://localhost:8081/scm/api/v2/config/jenkins
```

Whenever you change something on the master branch, a build should be triggered in Jenkins.
