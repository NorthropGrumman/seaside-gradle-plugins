package com.ngc.seaside.gradle.tasks.bats

import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
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
