package com.ngc.seaside.gradle.plugins.cpp.dependencies;

import org.gradle.api.Project;

import groovy.lang.Closure;

/**
 * The configuration for the {@link com.ngc.seaside.gradle.plugins.cpp.parent.SeasideCppParentPlugin}.
 * This configuration allows you to specify libraries within your dependencies with more fine grained control of the
 * linking arguments and headers.
 */
public class BuildingExtension {
   private BuildingExtensionDataStore storage = new BuildingExtensionDataStore();
   private Project project;

   /**
    * Constructor requiring the project.
    *
    * @param project the project
    */
   public BuildingExtension(Project project) {
      this.project = project;
   }

   public BuildingExtensionDataStore getStorage() {
      return storage;
   }
   
   public BuildingExtension setStorage(BuildingExtensionDataStore storage) {
      this.storage = storage;
      return this;
   }
   
   /**
    * The headers with the closure only. This requires that you set the dependency name within the closure.
    *
    * @param closure the Groovy closure that maps to a {@link HeaderBuildConfiguration}
    */
   public void headers(Closure<?> closure) {
      headers("", closure);
   }

   /**
    * The header setting the dependency name for convenience
    *
    * @param dependencyName the name of the dependency (i.e. the artifact Id)
    * @param closure        the Groovy closure that maps to a {@link HeaderBuildConfiguration}
    */
   public void headers(String dependencyName, Closure<?> closure) {
      HeaderBuildConfiguration headers = new HeaderBuildConfiguration();
      headers.dependency(dependencyName);
      project.configure(headers, closure);
      storage.add(headers);
   }

   /**
    * Add a dependency as an api project.
    *
    * @param dependencyName the name of the dependency (i.e. the artifact Id).
    */
   public void api(String dependencyName) {
      storage.addApi(dependencyName);
   }

   /**
    * Add a dependency that should be linked statically. Using this method assumes that the library has the same name
    * as the dependency.
    *
    * @param dependencyName the dependency name (i.e. the artifact Id).
    */
   public void statically(String dependencyName) {
      statically(dependencyName, null);
   }

   /**
    * Add a statically linked dependency using the closure only.
    * This method assumes that you will set the dependency name within the closure.
    *
    * @param closure the Groovy closure that maps to a {@link StaticBuildConfiguration}
    */
   public void statically(Closure<?> closure) {
      statically("", closure);
   }

   /**
    * Add a statically linked dependency by name.
    *
    * @param dependencyName the name of the dependency (i.e. the artifact Id).
    * @param closure the Groovy closure that maps to a {@link StaticBuildConfiguration}
    */
   public void statically(String dependencyName, Closure<?> closure) {
      StaticBuildConfiguration statically = new StaticBuildConfiguration(this.project);
      statically.setDependency(dependencyName);
      if (closure != null) {
         project.configure(statically, closure);
      }
      storage.add(statically);
   }

   /**
    * Add a shared dependency. Using this method assumes that the library has the same name
    * as the dependency.
    *
    * @param dependencyName the dependency name (i.e. the artifact Id).
    */
   public void shared(String dependencyName) {
      shared(dependencyName, null);
   }

   /**
    * Add a shared dependency using the closure only.
    * This method assumes that you will set the dependency name within the closure.
    *
    * @param closure the Groovy closure that maps to a {@link SharedBuildConfiguration}
    */
   public void shared(Closure<?> closure) {
      shared("", closure);
   }

   /**
    * Add a shared linked dependency by name.
    *
    * @param dependencyName the name of the dependency (i.e. the artifact Id).
    * @param closure the Groovy closure that maps to a {@link SharedBuildConfiguration}
    */
   public void shared(String dependencyName, Closure<?> closure) {
      SharedBuildConfiguration shared = new SharedBuildConfiguration(this.project);
      shared.setDependency(dependencyName);
      if (closure != null) {
         project.configure(shared, closure);
      }
      storage.add(shared);
   }

   /**
    * Convenience method that allows us to reference a dependency with the artifact Id
    * and find it in the project.
    *
    * @param path the path to the dependency.
    * @return the fully qualified path of the artifact (i.e. ${groupId}.${path}).
    */
   public String project(String path) {
      String p = path.startsWith(":") ? path.substring(1) : path;
      return project.getGroup().toString() + "." + p;
   }

}
