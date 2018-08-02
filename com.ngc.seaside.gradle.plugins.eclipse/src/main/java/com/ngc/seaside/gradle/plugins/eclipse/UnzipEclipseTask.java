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
