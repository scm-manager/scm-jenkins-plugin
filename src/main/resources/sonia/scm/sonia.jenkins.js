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


Ext.ns('Sonia.jenkins');

Sonia.jenkins.ConfigPanel = Ext.extend(Sonia.repository.FormPanel, {
  
  formTitleText: 'Jenkins',
  urlText: 'Url',
  projectText: 'Project',
  tokenText: 'Token',
  
  initComponent: function(){
    var url = "";
    var project = "";
    var token = "";
    
    // read fields from properties
    var properties = this.item.properties;
    if ( properties ){
      for (var i=0; i<properties.length; i++){
        var property = properties[i];
        if ( property.key == 'jenkins.url' ){
          url = property.value;
        } else if (property.key == 'jenkins.project'){
          project = property.value;
        } else if (property.key == 'jenkins.token'){
          token = property.value;
        }
      }
    }
    
    var config = {
      title: this.formTitleText,
      items: [{
        id: 'jenkinsUrl',
        fieldLabel: this.urlText,
        submitValue: false,
        value: url
      },{
        id: 'jenkinsProject',
        fieldLabel: this.projectText,
        submitValue: false,
        value: project
      },{
        id: 'jenkinsToken',
        fieldLabel: this.tokenText,
        submitValue: false,
        value: token
      }],
      listeners: {
        preUpdate: {
          fn: this.updateProperties,
          scope: this
        }
      }
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.jenkins.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }, 
  
  updateProperties: function(item){
    // create properties if they are empty
    if (!item.properties){
      item.properties = [];
    }
    
    // copy fields to properties
    item.properties.push({
      key: 'jenkins.url',
      value: item.jenkinsUrl
    },{
      key: 'jenkins.project',
      value: item.jenkinsProject
    },{
      key: 'jenkins.token',
      value: item.jenkinsToken
    });
    
    // remove properties from object
    delete item.jenkinsUrl;
    delete item.jenkinsProject;
    delete item.jenkinsToken;
  }
  
});

// register xtype
Ext.reg("jenkinsConfigPanel", Sonia.jenkins.ConfigPanel);

// register panel
Sonia.repository.openListeners.push(function(repository, panels){
  panels.push({
    xtype: 'jenkinsConfigPanel',
    item: repository
  });
});
