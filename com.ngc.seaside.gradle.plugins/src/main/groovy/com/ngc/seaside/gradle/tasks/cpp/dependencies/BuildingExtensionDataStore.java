package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * The storage of data for the {@link BuildingExtension} configuration.
 */
public class BuildingExtensionDataStore {

   private static Multimap<String, StaticBuildConfiguration> staticDependenciesMap = ArrayListMultimap.create();
   private static Multimap<String, SharedBuildConfiguration> sharedDependenciesMap = ArrayListMultimap.create();
   private static Multimap<String, HeaderBuildConfiguration> headersByDependencyMap = ArrayListMultimap.create();
   private static List<String> apiDependencies = new ArrayList<>();
   private static Map<String, StaticBuildConfiguration.WithArgs> librariesWithLinkerArgsMap = new HashMap<>();

   /**
    * Add the header configuration.
    *
    * @param headerBuildConfiguration the header configuration.
    */
   public void add(HeaderBuildConfiguration headerBuildConfiguration) {
      headersByDependencyMap.put(headerBuildConfiguration.getDependencyName(), headerBuildConfiguration);
   }

   /**
    * Get all header configurations for a given dependency.
    *
    * @param dependencyName the name of the dependency.
    * @return the configurations or null if not found.
    */
   public Collection<HeaderBuildConfiguration> getHeaderBuildConfigurations(String dependencyName) {
      return headersByDependencyMap.get(dependencyName);
   }

   /**
    * Add the dependency as an api
    *
    * @param dependencyName the name of the dependency.
    */
   public void addApi(String dependencyName) {
      apiDependencies.add(dependencyName);
   }

   /**
    * Get all of the api dependencies.
    *
    * @return the list of dependencies or an empty list if no configured apis exists.
    */
   public List<String> getApiDependencies() {
      return apiDependencies;
   }

   /**
    * Add a static configuration
    *
    * @param staticBuildConfiguration the static configuration
    */
   public void add(StaticBuildConfiguration staticBuildConfiguration) {
      staticDependenciesMap.put(staticBuildConfiguration.getDependency(), staticBuildConfiguration);
   }

   /**
    * Get all of the static dependencies by name.
    *
    * @return the static dependencies.
    */
   public Collection<String> getStaticDependencies() {
      return staticDependenciesMap.keySet();
   }

   /**
    * Get the static configuration by dependency name.
    *
    * @param dependencyName the name of the dependency.
    * @return the static dependencies or null if the dependency doesn't exists.
    */
   public Collection<StaticBuildConfiguration> getStaticBuildConfigurations(String dependencyName) {
      return staticDependenciesMap.get(dependencyName);
   }

   /**
    * Add a shared build configuration.
    *
    * @param sharedBuildConfiguration the shared build configuration
    */
   public void add(SharedBuildConfiguration sharedBuildConfiguration) {
      sharedDependenciesMap.put(sharedBuildConfiguration.getDependency(), sharedBuildConfiguration);
   }

   /**
    * Get the shared dependency names.
    *
    * @return the dependencies that have shared link configuration
    */
   public Collection<String> getSharedDependencies() {
      return sharedDependenciesMap.keySet();
   }

   /**
    * Get the shared build configuration by name.
    *
    * @param dependencyName the name of the dependency.
    * @return the shared dependency configurations or null if the dependency doesn't exist.
    */
   public Collection<SharedBuildConfiguration> getSharedBuildConfigurations(String dependencyName) {
      return sharedDependenciesMap.get(dependencyName);
   }

   /**
    * Get the linker arguments by file name.
    *
    * @param fileName the name of the library file.
    * @return the arguments or null if not found.
    */
   public StaticBuildConfiguration.WithArgs getLinkerArgs(String fileName) {
      return librariesWithLinkerArgsMap.get(fileName);
   }

   /**
    * Determine if the library has extra linker arguments configured.
    *
    * @param libraryName the name of the library.
    * @return true if the configuration contains withArgs for the given library name.
    */
   public boolean hasLinkArgs(String libraryName) {
      return librariesWithLinkerArgsMap.containsKey(libraryName);
   }

   /**
    * Add the library and the withArgs optional link arguments.
    *
    * @param libraryName the name of the library.
    * @param args        the arguments.
    */
   public void addLinkArgs(String libraryName, StaticBuildConfiguration.WithArgs args) {
      librariesWithLinkerArgsMap.put(libraryName, args);
   }

   /**
    * Get all the name of the libraries that have linker arguments.
    *
    * @return the library names.
    */
   public Set<String> getFilesWithLinkerArgs() {
      return librariesWithLinkerArgsMap.keySet();
   }
}
