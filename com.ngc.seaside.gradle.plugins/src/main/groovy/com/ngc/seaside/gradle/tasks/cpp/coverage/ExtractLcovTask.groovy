package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class ExtractLcovTask extends DefaultTask {
   private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                  .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def extractLcov() {
      def lcovFiles = extractTheLcovReleaseArchive()
      def outputDir = pathToTheDirectoryWithLcovFiles()

      project.copy {
         from lcovFiles
         into outputDir
      }
   }

   private FileTree extractTheLcovReleaseArchive() {
      return project.zipTree(pathToTheLcovReleaseArchive())
   }

   private String pathToTheLcovReleaseArchive() {
      return Paths.get(lcovReleaseArchiveFile())
   }

   private String lcovReleaseArchiveFile() {
      return projectClasspathConfiguration().filter { file ->
         return file.getName().contains("lcov")
      }.getAsPath()
   }

   private Configuration projectClasspathConfiguration() {
      return project
               .rootProject
               .buildscript
               .configurations
               .getByName("classpath")
   }

   private String pathToTheDirectoryWithLcovFiles() {
      return cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_LCOV
   }
}
