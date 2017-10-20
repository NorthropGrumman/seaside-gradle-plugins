package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.tasks.cpp.coverage.ExtractLcovTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.GenerateCoverageDataTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.FilterCoverageDataTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateCoverageXmlTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateCoverageHtmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class SeasideCppCoveragePlugin implements Plugin<Project> {
   public static final String CPP_COVERAGE_EXTENSION_NAME = "seasideCppCov"
   public static final String CPP_COVERAGE_TASK_GROUP_NAME = "C++ Coverage"

   public static final String COVERAGE_TASK_NAME = "coverage"
   public static final String EXTRACT_LCOV_TASK_NAME = "extractLcov"
   public static final String GENERATE_COVERAGE_DATA_TASK_NAME = "genCoverageData"
   public static final String FILTER_COVERAGE_DATA_TASK_NAME = "filterCoverageData"
   public static final String GENERATE_COVERAGE_DATA_HTML_TASK_NAME = "coverageWithHtml"
   public static final String GENERATE_LCOV_XML_TASK_NAME = "coverageWithXml"
   public static final String GENERATE_FULL_COVERAGE_REPORT_TASK_NAME = "genFullCoverageReport"

   String coverageFilePath 
   String coverageXmlPath

   @Override
   void apply(Project p) {
      p.configure(p) {
         SeasideCppCoverageExtension e = createTheCppCoverageExtensionOnTheProject(p)
         initializeConfigurableCppCoverageExtensionProperties(e)

         task(
            EXTRACT_LCOV_TASK_NAME,
            type: ExtractLcovTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Extract the lcov release archive")

         task(
            GENERATE_COVERAGE_DATA_TASK_NAME,
            type: GenerateCoverageDataTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Generate preliminary coverage data and store in the specified directory",
            dependsOn: ["build", EXTRACT_LCOV_TASK_NAME])

         task(
            FILTER_COVERAGE_DATA_TASK_NAME,
            type: FilterCoverageDataTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Filter coverage data and store in the specified directory",
            dependsOn: GENERATE_COVERAGE_DATA_TASK_NAME)

         task(
            COVERAGE_TASK_NAME,
            description: "Generate and filter coverage data and store in the specified directory",
            dependsOn: FILTER_COVERAGE_DATA_TASK_NAME
         )

         task(
            GENERATE_COVERAGE_DATA_HTML_TASK_NAME,
            type: GenerateCoverageHtmlTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Generate html from the coverage data in the specified directory",
            dependsOn: COVERAGE_TASK_NAME)

         task(
            GENERATE_LCOV_XML_TASK_NAME,
            type: GenerateCoverageXmlTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Generate a cobertura xml file from the lcov coverage info.",
            dependsOn: COVERAGE_TASK_NAME)

         task(
            GENERATE_FULL_COVERAGE_REPORT_TASK_NAME,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Generate the cobertura and html reports.",
            dependsOn: [GENERATE_COVERAGE_DATA_HTML_TASK_NAME, GENERATE_LCOV_XML_TASK_NAME])

         p.afterEvaluate {
            p.dependencies {
               compile "$e.LCOV_GROUP_ARTIFACT_VERSION"
               compile "$e.LCOV_COBERTURA_GROUP_ARTIFACT_VERSION"
            }
         }
      }
   }

   private static createTheCppCoverageExtensionOnTheProject(Project p) {
      return p.extensions
              .create(CPP_COVERAGE_EXTENSION_NAME, SeasideCppCoverageExtension, p)
   }

   private initializeConfigurableCppCoverageExtensionProperties(SeasideCppCoverageExtension e) {
      e.coverageFilePath = coverageFilePath ?: e.coverageFilePath
      e.coverageXmlPath = coverageXmlPath ?: e.coverageXmlPath
   }
}
