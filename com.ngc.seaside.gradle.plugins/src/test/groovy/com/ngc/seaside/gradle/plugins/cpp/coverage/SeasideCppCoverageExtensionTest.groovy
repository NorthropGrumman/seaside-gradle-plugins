package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SeasideCppCoverageExtensionTest {
   private SeasideCppCoverageExtension extension
   private Project project = Mockito.mock(Project)
   private File file = Mockito.mock(File)

   @Before
   void before() {
      Mockito.when(project.buildDir).thenReturn(file)
      extension = new SeasideCppCoverageExtension(project)
   }

   @Test
   void cppCoveragePathsFieldIsNotNull() {
      Assert.assertNotNull(extension.CPP_COVERAGE_PATHS)
   }

   @Test
   void cppCoveragePathIsSetToDefault() {
      Assert.assertEquals(extension.coverageFilePath, extension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_FILE)
   }

   @Test
   void returnsCorrectPathToLcovExecutable() {
      def expected = [
            project.buildDir.absolutePath,
            "tmp",
            "lcov",
            "lcov-${extension.LCOV_VERSION}",
            "bin",
            "lcov"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.CPP_COVERAGE_PATHS.PATH_TO_THE_LCOV_EXECUTABLE)
   }

   @Test
   void returnsCorrectPathToTheDirectoryWithLcov() {
      def expected = [
            project.buildDir.absolutePath,
            "tmp",
            "lcov"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_LCOV)
   }

   @Test
   void returnsCorrectPathToDefaultCoverageFile() {
      def expected = [
            project.buildDir.absolutePath,
            "lcov",
            "coverage.info"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_FILE)
   }
}
