package com.ngc.seaside.gradle.plugins.cpp.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows you to specify the headers by dependency.
 */
public class HeaderBuildConfiguration {

   private String dependencyName;
   private List<String> dirs = new ArrayList<>();

   /**
    * Constructor.
    */
   public HeaderBuildConfiguration() {}

   /**
    * Get the dependency name.
    *
    * @return the dependency.
    */
   public String getDependencyName() {
      return dependencyName;
   }

   /**
    * Set the dependency
    *
    * @param dependencyName the dependency name
    */
   public void dependency(String dependencyName) {
      this.dependencyName = dependencyName;
   }

   /**
    * Get the directories.
    *
    * @return the directories.
    */
   public List<String> getDirs() {
      return dirs;
   }

   /**
    * Set the directories of the include. This is relative to the top-level of the unzipped dependency.
    *
    * @param dirs the directories
    */
   public void setDirs(List<String> dirs) {
      this.dirs = dirs;
   }

   /**
    * Set the directories of the include. This is relative to the top-level of the unzipped dependency.
    *
    * @param dirs the directories
    */
   public void dirs(String... dirs) {
      Collections.addAll(this.dirs, dirs);
   }
}
