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
      Mockito.when(project.projectDir).thenReturn(file)
      extension = new SeasideCppCoverageExtension(project)
   }

   @Test
   void returnsCorrectPathToDefaultCoverageFile() {
      def expected = [
            project.buildDir.absolutePath,
            "lcov",
            "coverage.info"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.coverageFilePath)
   }

   @Test
   void returnsCorrectPathToDefaultCppCheckXml() {
      def expected = [
              project.buildDir.absolutePath,
              "cppcheck",
              "cppcheck.xml"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.cppCheckXmlPath)
   }

   @Test
   void returnsCorrectPathToDefaultRatsXml() {
      def expected = [
              project.buildDir.absolutePath,
              "rats",
              "rats-report.xml"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.CPP_COVERAGE_PATHS.PATH_TO_RATS_XML)
   }

   @Test
   void returnsCorrectPathToDefaultRatsHtml() {
      def expected = [
              project.buildDir.absolutePath,
              "reports",
              "rats",
              "html",
              "rats.html"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.CPP_COVERAGE_PATHS.PATH_TO_RATS_HTML)
   }

   @Test
   void returnsCorrectPathToDefaultLcovCoberturaXml() {
      def expected = [
              project.buildDir.absolutePath,
              "lcov",
              "coverage.xml"
      ].join(File.separator)
      Assert.assertEquals(expected, extension.coverageXmlPath)
   }
}
