package com.ngc.seaside.gradle.tasks.cpp.coverage.reports

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateCoverageDataHtmlTask extends DefaultTask {
    private SeasideCppCoverageExtension cppCoverageExtension =
            project.extensions
                   .findByType(SeasideCppCoverageExtension.class)

   @TaskAction
   def generateLcovHtml() {
      def genhtml = cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_GENHTML_EXECUTABLE
      def coverageHtmlDir = new File(cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_HTML_DIR)
      def coverageFile = new File(cppCoverageExtension.coverageFilePath)

      def arguments = [
         "-o", coverageHtmlDir.absolutePath,
         "-t", "${project.projectDir.name}",
         "--demangle-cpp",
         "--branch-coverage",
         "--function-coverage",
         "--legend",
         "--num-spaces", "4", 
         coverageFile
      ]

      if (coverageFile.exists()) {
         project.exec {
            executable genhtml
            args arguments
         }
      }
   }
}
