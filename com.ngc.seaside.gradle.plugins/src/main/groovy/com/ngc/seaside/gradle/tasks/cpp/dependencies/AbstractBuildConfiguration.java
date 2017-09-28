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
   private List<String> libs = new ArrayList<>();

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

   @Override
   public String toString() {
      return ", dependency='" + dependency + '\'' +
             ", libs=" + libs;
   }

   protected Project getProject() {
      return project;
   }
}
