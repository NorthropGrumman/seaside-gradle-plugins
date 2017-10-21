package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.plugins.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideCppCoveragePluginFT {
   private final String SUBPROJECT_DIR_PREFIX = "com.ngc.blocs.cpp."

   private SeasideCppCoverageExtension coverageExtension
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
      testProjectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
      )
      project = TestingUtilities.createTheTestProjectWith(testProjectDir)
   }

   @Test
   void doesExtractLcov() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.EXTRACT_COVERAGE_TOOLS_TASK_NAME)
      checkForTheExtractedLcovFiles()
   }

   @Test
   void doesGenerateCoverageData() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_COVERAGE_DATA_TASK_NAME)
      checkForTheCoverageFile()
   }

   @Test
   void doesFilterCoverageData() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.FILTER_COVERAGE_DATA_TASK_NAME)
      checkForTheCoverageFile()
   }

   @Test
   void doesGenerateCoverageDataHtml() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_COVERAGE_HTML_TASK_NAME)
      checkForTheHtmlFile()
   }

   @Test
   void doesGenerateCoverageXML() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_COVERAGE_XML_TASK_NAME)
      checkForTheXMLFile()
   }

   @Test
   void doesGenerateFullReport() {
      checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_FULL_COVERAGE_REPORT_TASK_NAME)
      checkForTheExtractedLcovFiles()
      checkForTheCoverageFile()
      checkForTheHtmlFile()
      checkForTheXMLFile()
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "pipeline-test-cpp"
      )
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
            "build", "functionalTest",
            "cpp", "coverage", "pipeline-test-cpp"
      )
   }

   private void checkForTaskSuccess(String taskName) {
      BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments(taskName)
            .build()

      subprojectNames.each { subprojectName ->
         TestingUtilities.assertTaskSuccess(result, subprojectName, taskName)
      }
   }

   private void checkForTheExtractedLcovFiles() {
      subprojectNames.each { subprojectName ->
         def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
         if (isSubproject(file)) {
            coverageExtension = createAnExtensionOnTheSubproject(file)
            def f = new File(coverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_DIRECTORY_WITH_LCOV)
            Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
         }
      }
   }

   private checkForTheCoverageFile() {
      subprojectNames.each { subprojectName ->
         def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
         if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0]))
            return

         if (isSubproject(file)) {
            coverageExtension = createAnExtensionOnTheSubproject(file)
            def f = new File(coverageExtension.coverageFilePath)
            Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
            Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
         }
      }
   }

   private checkForTheHtmlFile() {
      subprojectNames.each { subprojectName ->
         def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
         if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0]))
            return

         if (isSubproject(file)) {
            coverageExtension = createAnExtensionOnTheSubproject(file)
            def f = new File(coverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_HTML_DIR + "/index.html")
            Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
            Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
         }
      }
   }

   private checkForTheXMLFile() {
      subprojectNames.each { subprojectName ->
         def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
         if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0]))
            return

         if (isSubproject(file)) {
            coverageExtension = createAnExtensionOnTheSubproject(file)
            def f = new File(coverageExtension.coverageXmlPath)
            Assert.assertTrue("The file does not exist: ${f.absolutePath}",f.exists())
            Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
         }
      }
   }

   private boolean isSubproject(File file) {
      return file.directory && file.name.startsWith(SUBPROJECT_DIR_PREFIX)
   }

   private SeasideCppCoverageExtension createAnExtensionOnTheSubproject(File file) {
      def subproject = TestingUtilities.createSubprojectWithDir(project, file)
      coverageExtension = new SeasideCppCoverageExtension(subproject)
      return coverageExtension
   }
}
