/* *
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


Ext.ns("Sonia.jenkins");

Sonia.jenkins.GlobalConfigPanel = Ext.extend(Sonia.config.ConfigForm, {

  titleText: 'Jenkins Configuration',
  
  urlText: 'Url',
  repositoryConfigurationText: 'Do not allow repository configuration',
  triggerGitText: 'Disable Git trigger',
  triggerMercurialText: 'Disable Mercurial trigger',
  
  urlHelpText: 'Url of Jenkins installation (with contextpath).',
  repositoryConfigurationHelpText: 'Do not allow repository owners to configure jenkins hooks. \n\
    You have to restart your application server after changing this value.',
  triggerGitHelpText: 'Disable trigger for git repositories. \n\
    Note: the git trigger requires version 1.1.14 or above of the jenkins git plugin.',
  triggerMercurialHelpText: 'Disable trigger for mercurial repositories. \n\
    Note: the mercurial trigger requires version 1.38 or above of the jenkins mercurial plugin.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      items : [{
        xtype : 'textfield',
        fieldLabel : this.urlText,
        name : 'url',
        vtype: 'jenkinsUrl',
        allowBlank : true,
        helpText: this.urlHelpText
      },{
        xtype: 'checkbox',
        fieldLabel : this.repositoryConfigurationText,
        name: 'disable-repository-configuration',
        inputValue: 'true',
        helpText: this.repositoryConfigurationHelpText
      },{
        xtype: 'checkbox',
        fieldLabel : this.triggerGitText,
        name: 'disable-git-trigger',
        inputValue: 'true',
        helpText: this.triggerGitHelpText
      },{
        xtype: 'checkbox',
        fieldLabel : this.triggerMercurialText,
        name: 'disable-mercurial-trigger',
        inputValue: 'true',
        helpText: this.triggerMercurialHelpText
      }]
    }

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.jenkins.GlobalConfigPanel.superclass.initComponent.apply(this, arguments);
  },

  onSubmit: function(values){
    this.el.mask(this.submitText);
    Ext.Ajax.request({
      url: restUrl + 'plugins/jenkins/global-config.json',
      method: 'POST',
      jsonData: values,
      scope: this,
      disableCaching: true,
      success: function(response){
        this.el.unmask();
      },
      failure: function(){
        this.el.unmask();
      }
    });
  },

  onLoad: function(el){
    var tid = setTimeout( function(){
      el.mask(this.loadingText);
    }, 100);
    Ext.Ajax.request({
      url: restUrl + 'plugins/jenkins/global-config.json',
      method: 'GET',
      scope: this,
      disableCaching: true,
      success: function(response){
        var obj = Ext.decode(response.responseText);
        this.load(obj);
        clearTimeout(tid);
        el.unmask();
      },
      failure: function(){
        el.unmask();
        clearTimeout(tid);
        alert('failure');
      }
    });
  }

});

// register xtype
Ext.reg("jenkinsGlobalConfigPanel", Sonia.jenkins.GlobalConfigPanel);

// regist config panel
registerGeneralConfigPanel({
  id: 'jenkinsGlobalConfigPanel',
  xtype: 'jenkinsGlobalConfigPanel'
});