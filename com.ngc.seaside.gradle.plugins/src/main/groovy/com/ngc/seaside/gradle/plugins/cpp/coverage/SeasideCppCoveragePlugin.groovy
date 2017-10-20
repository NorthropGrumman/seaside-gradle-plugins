package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.tasks.cpp.coverage.ExtractLcovTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.GenerateCoverageDataTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.FilterCoverageDataTask
import com.ngc.seaside.gradle.tasks.cpp.coverage.reports.GenerateCoverageDataHtmlTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class SeasideCppCoveragePlugin implements Plugin<Project> {
   public static final String CPP_COVERAGE_EXTENSION_NAME = "seasideCppCov"
   public static final String CPP_COVERAGE_TASK_GROUP_NAME = "C++ Coverage"
   public static final String EXTRACT_LCOV_TASK_NAME = "extractLcov"
   public static final String GENERATE_COVERAGE_DATA_TASK_NAME = "generateCoverageData"
   public static final String FILTER_COVERAGE_DATA_TASK_NAME = "filterCoverageData"
   public static final String GENERATE_COVERAGE_DATA_HTML_TASK_NAME = "generateCoverageDataHtml"

   String coverageFilePath

   @Override
   void apply(Project p) {
      p.configure(p) {
         SeasideCppCoverageExtension e = createTheCppCoverageExtensionOnTheProject(p)
         initializeConfigurableCppCoverageExtensionProperties(e)

         buildscript {
            configurations {
               classpath
            }
         }

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
            GENERATE_COVERAGE_DATA_HTML_TASK_NAME,
            type: GenerateCoverageDataHtmlTask,
            group: CPP_COVERAGE_TASK_GROUP_NAME,
            description: "Generate html from the coverage data in the specified directory",
            dependsOn: FILTER_COVERAGE_DATA_TASK_NAME)

         p.afterEvaluate {
            p.dependencies {
               compile "lcov:lcov:$e.LCOV_VERSION"
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
   }
}
