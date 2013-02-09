/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

/** 
 * Register extjs namespace for the plugin.
 * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-ns
 */
Ext.ns('Sonia.jenkins');

/**
 * Create new component which extends the Sonia.repository.PropertiesFormPanel.
 * This panel should be visible as tab at the bottom of the repository overview
 * when a repository is selected and the current user is the admin of this 
 * repository. This panel display a formular for the configuration of the 
 * jenkins plugin. The setting of this formular is stored on the selected 
 * repository.
 */
Sonia.jenkins.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  
  /**
   * Labels and texts are stored as class properties
   * for localisation.
   */
  
  // title of config tab
  formTitleText: 'Jenkins',
  // label of the url field
  urlText: 'Url',
  // label of the project field.
  projectText: 'Project',
  // label of the token field
  tokenText: 'Token',
  // label of the username field.
  usernameText: 'Username',
  // label of the apiToken field.
  apiTokenText: 'API Token',
  
  // help text for the url field
  urlHelpText: 'Url of Jenkins installation (with contextpath).',
  // help text for the project field
  projectHelpText: 'The name of the Jenkins project.',
  // help text for the token field
  tokenHelpText: 'Jenkins Authentication Token',
  // help text for the username field
  usernameHelpText: 'Username which is used for the authentication on the Jenkins ci server.',
  // help text for the apiToken field
  apiTokenHelpText: 'The API Token of the user. This token is used for authentication. \n\
                     You could get your API Token from your Jenkins Server at \n\
                     http://yourjenkinsserver/jenkins/user/username/configure.',
  
  /**
   * This method initializes the component and configures it. 
   */
  initComponent: function(){
    
    /**
     * Config object to configure the panel and
     * its children.
     */
    var config = {
      // Title of the config panel.
      title: this.formTitleText,
      // formular elements
      items: [{
        // name of the field
        name: 'jenkinsUrl',
        // label of the field
        fieldLabel: this.urlText,
        /**
         * Mapped property of the field. This is the key of the property stored
         * on the repository object.
         */
        property: 'jenkins.url',
        // validation of the field
        vtype: 'jenkinsUrl',
        // help text
        helpText: this.urlHelpText
      },{
        // jenkins project field
        name: 'jenkinsProject',
        fieldLabel: this.projectText,
        property: 'jenkins.project',
        helpText: this.projectHelpText
      },{
        // jenkins token field
        name: 'jenkinsToken',
        fieldLabel: this.tokenText,
        property: 'jenkins.token',
        helpText: this.tokenHelpText
      },{
        // jenkins username field
        name: 'jenkinsUsername',
        fieldLabel: this.usernameText,
        property: 'jenkins.username',
        helpText: this.usernameHelpText
      },{
        // jenkins api token field
        name: 'jenkinsApiToken',
        fieldLabel: this.apiTokenText,
        property: 'jenkins.api-token',
        helpText: this.apiTokenHelpText
      }]
    };
    
    /**
     * The apply method merges the initialConfig object with the config object.
     * The initialConfig object is the config object from the parent panel 
     * (in this case Sonia.repository.PropertiesFormPanel).
     * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-apply
     */
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    
    /**
     * Call the initComponent method of the parent panel 
     * (Sonia.repository.PropertiesFormPanel)).
     */
    Sonia.jenkins.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }
  
});

/**
 * Register xtype of the panel for later use and lazy initialization.
 * http://docs.sencha.com/ext-js/3-4/#!/api/Ext-method-reg
 */
Ext.reg("jenkinsConfigPanel", Sonia.jenkins.ConfigPanel);

/**
 * Register a listener which is called, after repository is selected in the 
 * web interface. The listener becomes the selected repository and an array
 * of panels as argument.
 */ 
Sonia.repository.openListeners.push(function(repository, panels){
  
  /**
   * check if the current user is the owner of the repository
   */
  if (Sonia.repository.isOwner(repository)){
    /**
     * Append the jenkins config panel to the panels array.
     */
    panels.push({
      // registerd xtype for the panel
      xtype: 'jenkinsConfigPanel',
      /**
       * Selected repository. This parameter is required by the parent panel, 
       * Sonia.repository.PropertiesFormPanel.
       */
      item: repository
    });
  }
});
