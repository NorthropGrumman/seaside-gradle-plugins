package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractBuildConfiguration {
   private Project project;
   private String dependency;
   private String version; //optional
   private List<String> libs = new ArrayList<>(); //optional

   protected AbstractBuildConfiguration(Project project) {
      this.project = project;
   }

   public String getDependency() {
      return dependency;
   }

   public void setDependency(String dependency) {
      this.dependency = dependency;
   }

   public List<String> getLibs() {
      return libs;
   }

   public void setLibs(List<String> libs) {
      this.libs = libs;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   @Override
   public String toString() {
      return "dependency='" + dependency + '\'' +
             ", version='" + version + '\'' +
             ", libs=" + libs;
   }

   protected Project getProject() {
      return project;
   }
}
