package com.ngc.seaside.gradle.extensions.distribution;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeasideFelixServiceDistributionExtension {

   public static final String NAME = "felixService";

   private final Project project;
   private String distributionName;
   private File configFile;
   private Map<String, String> systemProperties = new LinkedHashMap<>();
   private List<String> programArgs = new ArrayList<>();
   private final ScriptConfiguration scriptHandler;

   public SeasideFelixServiceDistributionExtension(Project project) {
      this.project = project;
      this.scriptHandler = new ScriptConfiguration(project);
   }

   /**
    * Returns the object for getting and setting the script files.
    * 
    * @return the object for getting and setting the script files
    */
   public ScriptConfiguration getScripts() {
      return scriptHandler;
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
}
