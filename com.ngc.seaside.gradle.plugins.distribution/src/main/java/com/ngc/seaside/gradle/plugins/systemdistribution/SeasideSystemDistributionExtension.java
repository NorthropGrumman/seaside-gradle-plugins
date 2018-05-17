package com.ngc.seaside.gradle.plugins.systemdistribution;

import com.ngc.seaside.gradle.plugins.distribution.ScriptConfiguration;

import org.gradle.api.Action;
import org.gradle.api.Project;

import groovy.lang.Closure;

public class SeasideSystemDistributionExtension {

   public static final String NAME = "systemDistribution";
   
   private final Project project;
   private final ScriptConfiguration scriptConfiguration;
   private String distributionName;
   
   public SeasideSystemDistributionExtension(Project project) {
      this.project = project;
      this.scriptConfiguration = new ScriptConfiguration(project);
   }
   
   /**
    * Returns the object for getting and setting the script files.
    * 
    * @return the object for getting and setting the script files
    */
   public ScriptConfiguration getScripts() {
      return scriptConfiguration;
   }

   /**
    * Executes the given action against the script configuration.
    * 
    * @param action the action to execute to configure the script files
    * @return this
    */
   public SeasideSystemDistributionExtension scripts(Action<ScriptConfiguration> action) {
      action.execute(scriptConfiguration);
      return this;
   }

   /**
    * Calls the given closure against the script configuration.
    * 
    * @param c closure
    * @return this
    */
   public SeasideSystemDistributionExtension scripts(Closure<?> c) {
      c.call(scriptConfiguration);
      return this;
   }
   
   /**
    * The filename of the distribution archive, including the filetype. The default is
    * {@code <project.group>.<project.name>-<project.version>.zip}.
    * 
    * @return the name of the distribution archive
    */
   public String getDistributionName() {
      if (distributionName == null) {
         return project.getGroup() + "." + project.getName() + "-" + project.getVersion() + ".zip";
      }
      return distributionName;
   }

   /**
    * Sets the filename of the distribution archive.
    * 
    * @param distributionName the filename of the distribution archive
    * @return this
    */
   public SeasideSystemDistributionExtension setDistributionName(String distributionName) {
      this.distributionName = distributionName;
      return this;
   }
}
