package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.plugins.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideCppCoveragePluginIT {
   private SeasideCppCoveragePlugin plugin
   private Project project

   @Before
   void before() {
      def testProjectDir = createTheTestProjectDirectory()
      copyTheTestProjectInto(testProjectDir)
      project = createTheTestProjectUsing(testProjectDir)
      plugin = new SeasideCppCoveragePlugin()
      plugin.apply(project)
   }

   @Test
   void appliesPlugin() {
      Assert.assertNotNull(project.extensions.findByName(SeasideCppCoveragePlugin.CPP_COVERAGE_EXTENSION_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.EXTRACT_LCOV_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_COVERAGE_DATA_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.FILTER_COVERAGE_DATA_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.COVERAGE_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_COVERAGE_DATA_HTML_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_LCOV_XML_TASK_NAME))
      Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_FULL_COVERAGE_REPORT_TASK_NAME))
   }

   private static File createTheTestProjectDirectory() {
      def dir = pathToTheDestinationProjectDirectory()
      return Files.createDirectories(dir.toPath()).toFile()
   }

   private static File pathToTheDestinationProjectDirectory() {
      return turnListIntoPath(
            "build", "integrationTest", "resources",
            "cpp", "coverage", "sealion-java-hello-world"
      )
   }

   private static void copyTheTestProjectInto(File dir) {
      FileUtils.copyDirectory(sourceDirectoryWithTheTestProject(), dir)
   }

   private static File sourceDirectoryWithTheTestProject() {
      return turnListIntoPath(
            "src", "integrationTest", "resources",
            "sealion-java-hello-world"
      )
   }

   private static Project createTheTestProjectUsing(File dir) {
      return ProjectBuilder.builder().withProjectDir(dir).build()
   }

   private static File turnListIntoPath(String... list) {
      return Paths.get(list.flatten().join(File.separator)).toFile()
   }
}
