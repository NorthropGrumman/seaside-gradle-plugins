/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
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
