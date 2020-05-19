/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.systemdistribution;

import com.ngc.seaside.gradle.plugins.distribution.ScriptConfiguration;

import org.gradle.api.Action;
import org.gradle.api.Project;

import groovy.lang.Closure;

/**
 * Extension of the {@link SeasideSystemDistributionPlugin}. This extension allows you to change the distribution
 * name and modify the scripts used for starting the distribution.
 */
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
