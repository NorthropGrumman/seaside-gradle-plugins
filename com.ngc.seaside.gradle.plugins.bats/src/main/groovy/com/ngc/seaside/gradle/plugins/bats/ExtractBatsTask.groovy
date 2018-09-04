/*
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
package com.ngc.seaside.gradle.plugins.bats

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class ExtractBatsTask extends DefaultTask {
   private SeasideBatsExtension batsExtension =
            project.extensions
                   .getByType(SeasideBatsExtension.class)

   @TaskAction
   def extractBats() {
      def batsFiles = extractTheBatsReleaseArchive()
      def outputDir = pathToTheDirectoryWithBatsScripts()

      project.copy {
         from batsFiles
         into outputDir
      }
   }

   private FileTree extractTheBatsReleaseArchive() {
      return project.zipTree(pathToTheBatsReleaseArchive())
   }

   private String pathToTheBatsReleaseArchive() {
      return Paths.get(batsReleaseArchiveFile())
   }

   private String batsReleaseArchiveFile() {
      return projectClasspathConfiguration().filter { file ->
         return file.name.contains("bats")
      }.getAsPath()
   }

   private Configuration projectClasspathConfiguration() {
      return project
               .configurations
               .getByName("compile")
   }

   private String pathToTheDirectoryWithBatsScripts() {
      return batsExtension.BATS_PATHS.PATH_TO_THE_DIRECTORY_WITH_BATS_SCRIPTS
   }
}
