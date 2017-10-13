package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class FilterCoverageDataTask extends DefaultTask {
   SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def filterCoverageData() {
      def lcov = [project.buildDir.absolutePath, "tmp", "lcov", "lcov-$cppCoverageExtension.LCOV_VERSION", "bin", "lcov"].join(File.separator)
      def coverageFilePath = [project.buildDir.absolutePath, "lcov", "coverage.info"].join(File.separator)
      def arguments = ["-r", coverageFilePath, project.buildDir.absolutePath, "--rc", "lcov_branch_coverage=1", "-o", coverageFilePath]
      def commandOutput = new ByteArrayOutputStream()

      project.exec {
         workingDir environment.PWD.toString().trim()
         executable lcov
         args arguments
         standardOutput commandOutput
      }

      println commandOutput.toString()
   }
}
