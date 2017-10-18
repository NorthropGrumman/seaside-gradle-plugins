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
      testTask("extractLcov")
   }

   @Test
   void doesGenerateCoverageData() {
      testTask("generateCoverageData")
   }

   @Test
   void doesFilterCoverageData() {
      testTask("filterCoverageData")
   }
   
   @Test
   void doesGenerateCoverageDataHtml() {
      testTask("generateCoverageDataHtml")
   }
   
   private void testTask(final String taskName) { 
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", taskName)
            .build()

      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:"                         + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:"                   + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:"         + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:"   + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:"       + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:" + taskName).getOutcome())
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:"       + taskName).getOutcome())
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
