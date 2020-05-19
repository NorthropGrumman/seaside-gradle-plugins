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
package com.ngc.seaside.gradle.plugins.distribution;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.provider.SetProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import groovy.lang.Closure;

/**
 * Extension of the {@link SeasideFelixServiceDistributionPlugin}. This extension allows you to change the distribution
 * name, the {@code config.properties} file, add system properties and program arguments to the distribution executable,
 * modify the scripts used for starting the distribution, and adding more configurations for determining the
 * distribution's bundles.
 */
public class SeasideFelixServiceDistributionExtension {

   public static final String NAME = "felixService";

   private final Project project;
   private String distributionName;
   private File configFile;
   private Map<String, String> systemProperties = new LinkedHashMap<>();
   private List<String> programArgs = new ArrayList<>();
   private Set<Configuration> bundleConfigurations = new LinkedHashSet<>();
   private final ScriptConfiguration scriptConfiguration;
   private final SetProperty<String> blacklist;

   public SeasideFelixServiceDistributionExtension(Project project) {
      this.project = project;
      this.scriptConfiguration = new ScriptConfiguration(project);
      this.blacklist = project.getObjects().setProperty(String.class);
   }

   /**
    * Returns the set of bundle configurations.
    * 
    * @return the set of bundle configurations
    */
   public Set<Configuration> getBundleConfigurations() {
      return bundleConfigurations;
   }

   /**
    * Sets the set of bundle configurations to the given configurations. Note that unlike
    * {@link #bundleConfigurations(Iterable)} this replaces any previously defined includes. The elements can be
    * either of type {@link CharSequence} or {@link Configuration}.
    * 
    * @param configurations bundle configurations
    * @return this
    */
   public SeasideFelixServiceDistributionExtension setBundleConfigurations(Iterable<Object> configurations) {
      this.bundleConfigurations.clear();
      return bundleConfigurations(configurations);
   }

   /**
    * Adds the given configuration to the set of bundle configurations. The object can be either of type
    * {@link CharSequence} or {@link Configuration}.
    * 
    * @param configuration configuration to add
    * @return this
    */
   public SeasideFelixServiceDistributionExtension bundleConfiguration(Object configuration) {
      return bundleConfigurations(configuration);
   }

   /**
    * Adds the given configurations to the set of bundle configurations. The elements can be either of type
    * {@link CharSequence} or {@link Configuration}.
    * 
    * @param configurations configurations to add
    * @return this
    */
   public SeasideFelixServiceDistributionExtension bundleConfigurations(Object... configurations) {
      return bundleConfigurations(Arrays.asList(configurations));
   }

   /**
    * Adds the given configurations to the set of bundle configurations. The elements can be either of type
    * {@link CharSequence} or {@link Configuration}.
    * 
    * @param configurations configurations to add
    * @return this
    */
   public SeasideFelixServiceDistributionExtension bundleConfigurations(Iterable<Object> configurations) {
      for (Object configuration : configurations) {
         if (configuration instanceof CharSequence) {
            this.bundleConfigurations.add(project.getConfigurations().getByName(configuration.toString()));
         } else if (configuration instanceof Configuration) {
            this.bundleConfigurations.add((Configuration) configuration);
         } else {
            throw new UnknownConfigurationException("Unknown configuration: " + configuration);
         }
      }
      return this;
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
   public SeasideFelixServiceDistributionExtension scripts(Action<ScriptConfiguration> action) {
      action.execute(scriptConfiguration);
      return this;
   }

   /**
    * Calls the given closure against the script configuration.
    * 
    * @param c closure
    * @return this
    */
   public SeasideFelixServiceDistributionExtension scripts(Closure<?> c) {
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
   public SeasideFelixServiceDistributionExtension setDistributionName(String distributionName) {
      this.distributionName = distributionName;
      return this;
   }

   /**
    * Returns the framework configuration properties file.
    * 
    * @return the framework configuration properties file
    */
   public File getConfigFile() {
      return configFile;
   }

   /**
    * Sets the framework configuration properties file. This method converts the supplied parameter to a file using
    * {@link Project#file(Object)}.
    * 
    * @param configFile the framework configuration properties file
    * @return this
    */
   public SeasideFelixServiceDistributionExtension setConfigFile(Object configFile) {
      this.configFile = project.file(configFile);
      return this;
   }

   /**
    * Returns the JVM system properties applied when running the service distribution's executable.
    * 
    * @return the distribution's JVM system properties
    */
   public Map<String, String> getSystemProperties() {
      return Collections.unmodifiableMap(systemProperties);
   }

   /**
    * Sets the JVM system properties applied when running the service distribution's executable. Note that unlike
    * {@link #systemProperties(Map)} this replaces any previously defined system properties.
    * 
    * @param systemProperties the distribution's JVM system properties
    * @return this
    */
   public SeasideFelixServiceDistributionExtension setSystemProperties(Map<String, String> systemProperties) {
      this.systemProperties.clear();
      return systemProperties(systemProperties);
   }

   /**
    * Adds the given key and value to the JVM system properties applied when running the service distribution's
    * executable.
    * 
    * @param key system property key
    * @param value system property value
    * @return this
    */
   public SeasideFelixServiceDistributionExtension systemProperty(String key, String value) {
      return systemProperties(Collections.singletonMap(key, value));
   }

   /**
    * Adds the given map to the JVM system properties applied when running the service distribution's executable.
    * 
    * @param systemProperties system properties
    * @return this
    */
   public SeasideFelixServiceDistributionExtension systemProperties(Map<String, String> systemProperties) {
      systemProperties.forEach((key, value) -> {
         this.systemProperties.put(key, value);
      });
      return this;
   }

   /**
    * Returns the list of program arguments appended to the service distribution's executable.
    * 
    * @return the list of the service distribution's program arguments
    */
   public List<String> getProgramArgs() {
      return Collections.unmodifiableList(programArgs);
   }

   /**
    * Sets the list of program arguments appended to the service distribution's executable. Note that unlike
    * {@link #programArgs(Iterable)} this replaces any previously defined program arguments.
    * 
    * @param programArgs the list of the service distribution's program arguments
    * @return this
    */
   public SeasideFelixServiceDistributionExtension setProgramArgs(Iterable<String> programArgs) {
      this.programArgs.clear();
      return programArgs(programArgs);
   }

   /**
    * Adds the given argument to the list of program arguments appended to the service distribution's executable.
    * 
    * @param programArg argument to add to the service distribution's executable
    * @return this
    */
   public SeasideFelixServiceDistributionExtension programArg(String programArg) {
      return programArgs(programArg);
   }

   /**
    * Adds the given arguments to the list of program arguments appended to the service distribution's executable.
    * 
    * @param programArgs arguments to add to the service distribution's executable
    * @return this
    */
   public SeasideFelixServiceDistributionExtension programArgs(String... programArgs) {
      return programArgs(Arrays.asList(programArgs));
   }

   /**
    * Adds the given arguments to the list of program arguments appended to the service distribution's executable.
    * 
    * @param programArgs arguments to add to the service distribution's executable
    * @return this
    */
   public SeasideFelixServiceDistributionExtension programArgs(Iterable<String> programArgs) {
      for (String arg : programArgs) {
         this.programArgs.add(arg);
      }
      return this;
   }

   /**
    * Returns the list of system arguments as a single string of "-Dkey=value" strings.
    * 
    * @return the list of system arguments as a single string
    */
   public String getJvmArgumentsString() {
      return getSystemProperties().entrySet()
                                  .stream()
                                  .map(entry -> "-D" + entry.getKey() + "=" + entry.getValue())
                                  .collect(Collectors.joining(" "));
   }

   /**
    * Returns the list of program arguments as a single string.
    * 
    * @return the list of program arguments as a single string
    */
   public String getProgramArgumentsString() {
      return getProgramArgs().stream()
                             .collect(Collectors.joining(" "));
   }

   /**
    * Gets the GAVs of components that should not be included in the distribution.
    *
    * @return the GAVs of components that should not be included in the distribution
    */
   public SetProperty<String> getBlacklist() {
      return blacklist;
   }

   /**
    * Adds the given GAVs to the blacklist.  Components with these coordinates will not be included in the distribution.
    *
    * @param gavs the GAVs to add to the blacklist
    */
   public void blacklist(String... gavs) {
      Arrays.stream(gavs).forEach(blacklist::add);
   }

   /**
    * Sets the GAVs of components that should not be included in the distribution.
    *
    * @param blacklist the GAVs of components that should not be included in the distribution
    */
   public void setBlacklist(Collection<String> blacklist) {
      this.blacklist.set(blacklist);
   }

   /**
    * Sets the GAVs of components that should not be included in the distribution.
    *
    * @param blacklist the GAVs of components that should not be included in the distribution
    */
   public void setBlacklist(SetProperty<String> blacklist) {
      this.blacklist.set(blacklist);
   }
}
