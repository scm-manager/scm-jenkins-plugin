import jenkins.model.*;
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;

def global_domain = Domain.global()
def credentials_store = Jenkins.instance.getExtensionList(
  'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
)[0].getStore()

def credentials = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  'scmCredentials',
  'Credentials for SCM-Manager',
  'scmadmin',
  'scmadmin')

credentials_store.addCredentials(global_domain, credentials)
