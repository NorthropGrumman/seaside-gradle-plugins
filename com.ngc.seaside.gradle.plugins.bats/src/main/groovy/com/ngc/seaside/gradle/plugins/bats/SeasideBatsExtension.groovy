/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
