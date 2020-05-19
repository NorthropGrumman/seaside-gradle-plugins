/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
