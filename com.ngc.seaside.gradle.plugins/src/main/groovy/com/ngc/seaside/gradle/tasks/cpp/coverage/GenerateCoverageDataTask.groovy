package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateCoverageDataTask extends DefaultTask {
   private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def generateCoverageData() {
      def dir = project.projectDir.absolutePath
      def lcov = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_LCOV_EXECUTABLE
      def arguments = [
         "--no-external",
         "--base-directory", dir,
         "--directory", dir,
         "--rc", "lcov_branch_coverage=1",
         "-c", "-o", cppCoverageExtension.coverageFilePath
      ]

      createCoverageFilePath()

      project.exec {
         workingDir dir
         executable lcov
         args arguments
      }

      deleteCoverageFileIfEmpty()
   }

   private createCoverageFilePath() {
      (new File(cppCoverageExtension.coverageFilePath)).parentFile.mkdirs()
   }

   private deleteCoverageFileIfEmpty() {
      def f = new File(cppCoverageExtension.coverageFilePath)
      if (f.text.trim().empty)
         f.delete()
   }
}
