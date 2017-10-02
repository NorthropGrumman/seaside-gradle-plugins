package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 *
 */
public class BuildingExtensionDataStore {

   private static Multimap<String, StaticBuildConfiguration> staticDependenciesMap = ArrayListMultimap.create();
   private static Multimap<String, SharedBuildConfiguration> sharedDependenciesMap = ArrayListMultimap.create();
   private static Multimap<String, HeaderBuildConfiguration> headersByDependencyMap = ArrayListMultimap.create();
   private static List<String> apiDependencies = new ArrayList<>();
   private static Map<String, StaticBuildConfiguration.WithArgs> librariesWithLinkerArgsMap = new HashMap<>();
   private List<String> extraLinkArgs = new ArrayList<>();
   private List<String> extraCompileArgs = new ArrayList<>();

   public boolean hasLinkArgs(String libraryName) {
      return librariesWithLinkerArgsMap.containsKey(libraryName);
   }

   public void addExtraLinkArgs(List<String> args) {
      this.extraLinkArgs.addAll(args);
   }

   public List<String> getExtraLinkArgs() {
      return extraLinkArgs;
   }

   public void addExtraComileArgs(List<String> args) {
      this.extraCompileArgs.addAll(args);
   }

   public List<String> getExtraCompileArgs() {
      return extraCompileArgs;
   }

   public void addLinkArgs(String libraryName, StaticBuildConfiguration.WithArgs args) {
      librariesWithLinkerArgsMap.put(libraryName, args);
   }

   public Set<String> getFilesWithLinkerArgs() {
      return librariesWithLinkerArgsMap.keySet();
   }

   public StaticBuildConfiguration.WithArgs getLinkerArgs(String fileName) {
      return librariesWithLinkerArgsMap.get(fileName);
   }

   public void add(HeaderBuildConfiguration headerBuildConfiguration) {
      headersByDependencyMap.put(headerBuildConfiguration.getDependencyName(), headerBuildConfiguration);
   }

   public Collection<HeaderBuildConfiguration> getHeaderBuildConfigurations(String dependencyName) {
      return headersByDependencyMap.get(dependencyName);
   }

   public void addApi(String dependencyName) {
      apiDependencies.add(dependencyName);
   }

   public List<String> getApiDependencies() {
      return apiDependencies;
   }

   public void add(StaticBuildConfiguration staticBuildConfiguration) {
      staticDependenciesMap.put(staticBuildConfiguration.getDependency(), staticBuildConfiguration);
   }

   public Collection<String> getStaticDependencies() {
      return staticDependenciesMap.keySet();
   }

   public Collection<StaticBuildConfiguration> getStaticBuildConfigurations(String dependencyName) {
      return staticDependenciesMap.get(dependencyName);
   }

   public void add(SharedBuildConfiguration sharedBuildConfiguration) {
      sharedDependenciesMap.put(sharedBuildConfiguration.getDependency(), sharedBuildConfiguration);
   }

   public Collection<String> getSharedDependencies() {
      return sharedDependenciesMap.keySet();
   }

   public Collection<SharedBuildConfiguration> getSharedBuildConfigurations(String dependencyName) {
      return sharedDependenciesMap.get(dependencyName);
   }
}
