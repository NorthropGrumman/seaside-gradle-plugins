package com.ngc.seaside.gradle.plugins.cpp.coverage

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
import java.nio.file.Paths

class SeasideCppCoveragePluginFT {
   private SeasideCppCoveragePlugin plugin
   private File testProjectDir
   private Project project
   private List<File> pluginClasspath = getPluginClasspath()

   @Before
   void before() {
      testProjectDir = createTheTestProjectDirectory()
      copyTheTestProject()
      project = createTheTestProject()
      plugin = new SeasideCppCoveragePlugin()
      plugin.apply(project)
   }

   @Test
   void doesExtractLcov() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", "extractLcov")
            .build()

      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":logservice:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.api:extractLcov").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.utilities:extractLcov").getOutcome())
   }


   @Test
   void doesGenerateCoverageData() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", "generateCoverageData")
            .build()

      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":logservice:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.api:generateCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.utilities:generateCoverageData").getOutcome())
   }

   @Test
   void doesFilterCoverageData() {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", "filterCoverageData")
            .build()

      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":logservice:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.api:filterCoverageData").getOutcome())
      Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.utilities:filterCoverageData").getOutcome())
   }

   private List<File> getPluginClasspath() {
      URL r = getThePluginClassPathResource()
      throwIfTheClasspathResourceIsNotFound(r)
      return createNewFileForEachItemInClasspath(r)
   }

   private URL getThePluginClassPathResource() {
      return getClass().classLoader.getResource("plugin-classpath.txt")
   }

   private static void throwIfTheClasspathResourceIsNotFound(URL r) {
      if (!r)
         throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
   }

   private static List<File> createNewFileForEachItemInClasspath(URL r) {
      return r.readLines().collect { new File(it) }
   }

   private static File createTheTestProjectDirectory() {
      def dir = pathToTheDestinationProjectDirectory()
      return Files.createDirectories(dir.toPath()).toFile()
   }

   private static File pathToTheDestinationProjectDirectory() {
      return turnListIntoPath(
            "build", "functionalTest", "resources",
            "cpp", "coverage", "pipeline-test-cpp"
      )
   }

   private void copyTheTestProject() {
      FileUtils.copyDirectory(sourceDirectoryWithTheTestProject(), testProjectDir)
   }

   private static File sourceDirectoryWithTheTestProject() {
      return turnListIntoPath(
            "src", "functionalTest", "resources", "pipeline-test-cpp"
      )
   }

   private Project createTheTestProject() {
      return ProjectBuilder.builder().withProjectDir(testProjectDir).build()
   }

   private static File turnListIntoPath(String... list) {
      return Paths.get(list.flatten().join(File.separator)).toFile()
   }
}
