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
      def dir = project.projectDir.absolutePath
      def lcov = [project.buildDir.absolutePath, "tmp", "lcov", "lcov-$cppCoverageExtension.LCOV_VERSION", "bin", "lcov"].join(File.separator)
      def coverageFilePath = [project.buildDir.absolutePath, "lcov", "coverage.info"].join(File.separator)
      def arguments = "--no-external --base-directory $dir --directory $dir --rc lcov_branch_coverage=1 -c -o $coverageFilePath".split()

      def coverageFile = new File(coverageFilePath)
      coverageFile.parentFile.mkdirs()

      project.exec {
         workingDir dir
         executable lcov
         args arguments
      }

      if (coverageFile.text.trim().empty)
         coverageFile.delete()
   }
}
