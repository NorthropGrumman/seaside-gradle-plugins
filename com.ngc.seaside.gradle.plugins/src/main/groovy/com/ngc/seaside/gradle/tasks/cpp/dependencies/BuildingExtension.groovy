package com.ngc.seaside.gradle.tasks.cpp.dependencies

import org.gradle.api.Project

/**
 *
 */
class BuildingExtension {

   List<String> headers = []
   BuildingExtensionDataStore storage = new BuildingExtensionDataStore()
   Project project

   BuildingExtension(Project project) {
      this.project = project
   }

   void headers(Closure closure) {
      headers("", closure);
   }

   void headers(String dependencyName, Closure closure) {
      HeaderBuildConfiguration headers = new HeaderBuildConfiguration()
      headers.dependencyName = dependencyName
      project.configure(headers, closure);
      storage.add(headers);
   }

   void api(String dependencyName) {
      storage.addApi(dependencyName)
   }

   void statically(String dependencyName) {
      statically(dependencyName, null)
   }

   void statically(Closure closure) {
      statically("", closure)
   }

   void statically(String dependencyName, Closure closure) {
      StaticBuildConfiguration statically = new StaticBuildConfiguration(this.project)
      statically.dependency = dependencyName
      if (closure != null) {
         project.configure(statically, closure)
      }
      storage.add(statically)
   }

   void shared(String dependencyName) {
      shared(dependencyName, null)
   }

   void shared(Closure closure) {
      shared("", closure)
   }

   void shared(String dependencyName, Closure closure) {
      SharedBuildConfiguration shared = new SharedBuildConfiguration(this.project)
      shared.dependency = dependencyName
      if (closure != null) {
         project.configure(shared, closure)
      }
      storage.add(shared)
   }

   /**
    *
    * @param path
    * @return
    */
   String project(String path) {
      String p = path.startsWith(":") ? path.substring(1) : path;
      return project.getGroup().toString() + "." + p
   }

}
