package com.ngc.seaside.gradle.extensions.distribution;

import org.gradle.api.Project;
import org.gradle.internal.impldep.com.google.api.client.repackaged.com.google.common.base.Preconditions;

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
