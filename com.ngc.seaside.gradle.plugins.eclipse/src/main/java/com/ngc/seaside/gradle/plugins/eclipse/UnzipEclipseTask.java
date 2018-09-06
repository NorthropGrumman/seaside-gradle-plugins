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
            spec.into(unzippedDirectory.getParentFile());
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
