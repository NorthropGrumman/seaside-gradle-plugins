package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.plugins.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files

class SeasideCppCoveragePluginFT {
   private File testProjectDir
   private Project project
   private List<File> pluginClasspath

   @Before
   void before() {
      pluginClasspath = TestingUtilities.getTestClassPath(getClass())
      testProjectDir = setUpTheTestProjectDirectory()
      project = createTheTestProject()
   }

   @Test
   void doesExtractLcov() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments( "extractLcov")
            .build()

      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:extractLcov").getOutcome())
   }

   @Test
   void doesGenerateCoverageData() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("generateCoverageData")
            .build()

      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:generateCoverageData").getOutcome())
   }

   @Test
   void doesFilterCoverageData() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("filterCoverageData")
            .build()

      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:filterCoverageData").getOutcome())
   }

   @Ignore
   @Test
   void doesGenerateCoverageXml() {
      BuildResult result = GradleRunner.create()
              .withProjectDir(testProjectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("generateLcovXml")
              .build()

      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:generateLcovXml").getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:generateLcovXml").getOutcome())
   }

   private static File setUpTheTestProjectDirectory() {
      def dir = createTheTestProjectDirectory()
      copyTheTestProjectIntoTheTestProjectDirectory(dir)
      return dir
   }

   private static File createTheTestProjectDirectory() {
      def dir = pathToTheDestinationProjectDirectory()
      return Files.createDirectories(dir.toPath()).toFile()
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "resources",
            "cpp", "coverage", "pipeline-test-cpp"
      )
   }

   private static void copyTheTestProjectIntoTheTestProjectDirectory(File dir) {
      FileUtils.copyDirectory(sourceDirectoryWithTheTestProject(), dir)
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "pipeline-test-cpp"
      )
   }

   private Project createTheTestProject() {
      return ProjectBuilder.builder().withProjectDir(testProjectDir).build()
   }
}
