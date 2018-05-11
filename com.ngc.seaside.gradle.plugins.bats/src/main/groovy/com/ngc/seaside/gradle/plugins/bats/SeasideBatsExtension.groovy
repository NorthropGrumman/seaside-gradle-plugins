package com.ngc.seaside.gradle.plugins.bats

import org.gradle.api.Project

class SeasideBatsExtension {
   final String BATS_VERSION = "0.4.0"
   final BatsPaths BATS_PATHS

   String resultsFile
   String batsTestsDir

   SeasideBatsExtension(Project project) {
      BATS_PATHS = new BatsPaths(project, BATS_VERSION)
      resultsFile = BATS_PATHS.BATS_TEST_RESULTS_FILE
      batsTestsDir = BATS_PATHS.DIRECTORY_WITH_BATS_TESTS
   }

   private class BatsPaths {
      final String PATH_TO_THE_BATS_SCRIPT
      final String DIRECTORY_WITH_BATS_TESTS
      final String BATS_TEST_RESULTS_FILE
      final String PATH_TO_THE_DIRECTORY_WITH_BATS_SCRIPTS

      BatsPaths(Project project, String batsVersion) {
         PATH_TO_THE_BATS_SCRIPT = toPath(
            project.buildDir.name,
            "tmp",
            "bats-scripts",
            "bats-$batsVersion",
            "libexec",
            "bats")

         DIRECTORY_WITH_BATS_TESTS = toPath(
            "src",
            "test",
            "bats")

         BATS_TEST_RESULTS_FILE = toPath(
            project.buildDir.name,
            "test-results",
            "bats-tests",
            "results.out")

         PATH_TO_THE_DIRECTORY_WITH_BATS_SCRIPTS = toPath(
            project.buildDir.name,
            "tmp",
            "bats-scripts")
      }

   }
   private static String toPath(String... items) {
      return items.flatten().join(File.separator)
   }
}
