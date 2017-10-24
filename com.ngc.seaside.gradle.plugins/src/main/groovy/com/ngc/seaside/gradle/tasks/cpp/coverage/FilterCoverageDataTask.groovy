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
      def coverageFile = new File(cppCoverageExtension.coverageFilePath)
      def arguments = [
         "-r", coverageFile,
         "$project.projectDir.name/$project.buildDir.name/*",
         "--rc", "lcov_branch_coverage=1",
         "-o", coverageFile
      ]

      if (coverageFile.exists()) {
         project.exec {
            executable lcov
            args arguments
         }
      }
   }
}
