package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class FilterCoverageDataTask extends DefaultTask {
   private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def filterCoverageData() {
      def lcov = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_LCOV_EXECUTABLE
      def coverageFilePath = cppCoverageExtension.coverageFilePath
      def arguments = [
         "-r", coverageFilePath,
         "/$project.buildDir.name/*",
         "--rc", "lcov_branch_coverage=1",
         "-o", coverageFilePath
      ]

      if (new File(coverageFilePath).exists()) {
         project.exec {
            executable lcov
            args arguments
         }
      }
   }
}
