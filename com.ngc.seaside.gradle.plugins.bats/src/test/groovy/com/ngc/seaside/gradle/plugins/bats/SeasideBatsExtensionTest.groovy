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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SeasideBatsExtensionTest {
   private SeasideBatsExtension extension
   private Project project = Mockito.mock(Project)
   private File file = Mockito.mock(File)

   @Before
   void before() {
      Mockito.when(project.buildDir).thenReturn(file)
      extension = new SeasideBatsExtension(project)
   }

   @Test
   void batsPathsFieldIsNotNull() {
      Assert.assertNotNull(extension.BATS_PATHS)
   }

   @Test
   void returnsCorrectPathToTheBatsScript() {
      def expected = [
            project.buildDir.name,
            "tmp",
            "bats-scripts",
            "bats-${extension.BATS_VERSION}",
            "libexec",
            "bats" ].join(File.separator)
      Assert.assertEquals(expected, extension.BATS_PATHS.PATH_TO_THE_BATS_SCRIPT)
   }

   @Test
   void returnsCorrectPathToTheDefaultDirectoryWithTests() {
      def expected = [
            "src",
            "test",
            "bats" ].join(File.separator)
      Assert.assertEquals(expected, extension.BATS_PATHS.DIRECTORY_WITH_BATS_TESTS)
   }

   @Test
   void returnsCorrectPathToTheDefaultTestResultsFile() {
      def expected = [
            project.buildDir.name,
            "test-results",
            "bats-tests",
            "results.out" ].join(File.separator)
      Assert.assertEquals(expected, extension.BATS_PATHS.BATS_TEST_RESULTS_FILE)
   }
}
