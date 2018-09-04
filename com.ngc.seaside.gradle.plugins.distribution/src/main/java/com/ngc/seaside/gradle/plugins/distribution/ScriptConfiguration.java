/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.distribution;

import com.google.common.base.Preconditions;

import org.gradle.api.Project;

import java.io.File;

public class ScriptConfiguration {

   private final Project project;
   private File windowsScript;
   private File linuxScript;

   public ScriptConfiguration(Project project) {
      this.project = project;
   }

   /**
    * Returns the script for launching the service distribution in Windows.
    * 
    * @return the script for launching the service distribution in Windows
    */
   public File getWindowsScript() {
      return windowsScript;
   }

   /**
    * Returns the script for launching the service distribution in Linux.
    * 
    * @return the script for launching the service distribution in Linux
    */
   public File getLinuxScript() {
      return linuxScript;
   }

   /**
    * Sets the script for launching the service distribution in Windows. This method converts the supplied parameter to
    * a file using {@link Project#file(Object)}.
    * 
    * @param script the script for launching the service distribution in Windows
    * @return this
    */
   public ScriptConfiguration windows(Object script) {
      Preconditions.checkNotNull(script, "script cannot be null!");
      windowsScript = project.file(script);
      return this;
   }

   /**
    * Sets the script for launching the service distribution in Linux. This method converts the supplied parameter to
    * a file using {@link Project#file(Object)}.
    * 
    * @param script the script for launching the service distribution in Windows
    * @return this
    */
   public ScriptConfiguration linux(Object script) {
      Preconditions.checkNotNull(script, "script cannot be null!");
      linuxScript = project.file(script);
      return this;
   }

}
