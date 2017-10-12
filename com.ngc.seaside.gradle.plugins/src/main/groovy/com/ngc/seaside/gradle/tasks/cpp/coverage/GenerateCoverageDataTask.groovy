package com.ngc.seaside.gradle.tasks.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateCoverageDataTask extends DefaultTask {
   SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def generateCoverageData() {
      def commandOutput = new ByteArrayOutputStream()

      project.exec {
         executable "pwd"
         standardOutput commandOutput
      }

      def workingDir = commandOutput.toString()
      def lcov = [project.buildDir.name, "tmp", "lcov", "lcov-$cppCoverageExtension.LCOV_VERSION", "bin", "lcov"].join(File.separator)
      def coverageFilePath = [project.buildDir.name, "lcov", "coverage.info"].join(File.separator)
      def arguments = ["--no-external", "--base-directory", "$workingDir?", "--directory", "$workingDir?", "--rc", "lcov_branch_coverage=1", "-c", "-o", coverageFilePath]

      commandOutput = new ByteArrayOutputStream()

      project.exec {
         executable lcov
         args arguments
         standardOutput commandOutput
      }

      println commandOutput.toString()
   }
}
