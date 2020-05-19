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
