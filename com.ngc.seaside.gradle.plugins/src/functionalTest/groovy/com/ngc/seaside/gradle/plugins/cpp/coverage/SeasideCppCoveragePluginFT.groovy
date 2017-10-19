package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
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

import java.io.File
import java.nio.file.Files

class SeasideCppCoveragePluginFT {
   private File testProjectDir
   private Project project
   private List<File> pluginClasspath

   private List<String> subprojectNames = [
      "service.api",                         
      "service.utilities",
      "service.log.impl.logservice",         
      "service.thread.impl.threadservice",   
      "service.time.impl.timeservice",       
      "service.event.impl.synceventservice", 
      "service.log.impl.printservice"      
   ]

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

      subprojectNames.each { eachSubprojectName ->
         def eachSubprojectDirName = "com.ngc.blocs.cpp." + eachSubprojectName
         def subprojectDir = new File("${project.projectDir}/${eachSubprojectDirName}")
         def subproject = ProjectBuilder.builder().withProjectDir(subprojectDir).withParent(project).build()
         def cppCoverageExtension = new SeasideCppCoverageExtension(subproject)
         def coverageFile = new File(cppCoverageExtension.coverageFilePath)
         def htmlFile = new File(cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_HTML_DIR + "/index.html")

         if (coverageFile.exists()) {           
            Assert.assertTrue("The file does not exist: ${htmlFile.absolutePath}", htmlFile.exists())
         }
      }
   }
   
   private void testTask(final String taskName) { 
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", taskName)
            .build()

      subprojectNames.each { eachSubprojectName ->
         Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":${eachSubprojectName}:" + taskName).getOutcome())
      }
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
