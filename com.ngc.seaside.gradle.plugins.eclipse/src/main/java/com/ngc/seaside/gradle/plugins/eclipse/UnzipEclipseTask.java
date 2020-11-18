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
package com.ngc.seaside.gradle.plugins.eclipse;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Task for unzipping the downloaded Eclipse distribution.
 */
public class UnzipEclipseTask extends DefaultTask {

   /**
    * Used to set executable permissions on the eclipse executable.  Equivalent to unix file permissions: rwxr-xr-x.
    */
   private static final int UNIX_EXECUTABLE_PERMISSIONS = 493;

   private RegularFileProperty eclipseArchive;
   private DirectoryProperty unzippedDistributionDirectory;

   public UnzipEclipseTask() {
      this.eclipseArchive = newInputFile();
      this.unzippedDistributionDirectory = newOutputDirectory();
   }

   /**
    * Unzips the Eclipse SDK zip file, if an unzipped version of it doesn't already exist.
    */
   @TaskAction
   void unzipEclipse() {
      File distribution = eclipseArchive.get().getAsFile();
      File unzippedDirectory = unzippedDistributionDirectory.get().getAsFile();

      if (!unzippedDirectory.exists()) {
         getProject().getLogger().lifecycle("Unzipping Eclipse SDK from " + distribution + "...");
         getProject().copy(spec -> {
            spec.from(getProject().zipTree(distribution));
            spec.eachFile(file -> {
               if (file.getName().equals("eclipse")) {
                  file.setMode(UNIX_EXECUTABLE_PERMISSIONS);
               }
            });
            spec.into(unzippedDirectory);
         });
      }
   }

   /**
    * Returns the property of the Eclipse distribution archive zip file.
    *
    * @return the property of the Eclipse distribution archive zip file
    */
   @Internal
   public RegularFileProperty getEclipseArchive() {
      return eclipseArchive;
   }

   /**
    * Returns the property of the directory for the unzipped Eclipse distribution.
    *
    * @return the property of the directory for the unzipped Eclipse distribution
    */
   @Internal
   public DirectoryProperty getUnzippedDistributionDirectory() {
      return unzippedDistributionDirectory;
   }
}
