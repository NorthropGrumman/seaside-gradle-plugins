package com.ngc.seaside.gradle.tasks.cpp.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author justan.provence@ngc.com
 */
public class HeaderBuildConfiguration {

   private String dependencyName;
   private List<String> dirs = new ArrayList<>();

   public HeaderBuildConfiguration() {

   }

   public String getDependencyName() {
      return dependencyName;
   }

   public void dependency(String dependencyName) {
      this.dependencyName = dependencyName;
   }

   public List<String> getDirs() {
      return dirs;
   }

   public void setDirs(List<String> dirs) {
      this.dirs = dirs;
   }

   public void dirs(String... dirs) {
      Collections.addAll(this.dirs, dirs);
   }
}
