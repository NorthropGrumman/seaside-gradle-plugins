package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
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
