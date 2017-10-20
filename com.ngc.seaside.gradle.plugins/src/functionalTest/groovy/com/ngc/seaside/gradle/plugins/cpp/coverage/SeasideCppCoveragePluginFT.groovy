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
      checkForTaskSuccess("extractLcov")
      checkForTheExtractedLcovFiles()
   }

   @Test
   void doesGenerateCoverageData() {
      checkForTaskSuccess("generateCoverageData")
      checkForTheCoverageFile()
   }

   @Test
   void doesFilterCoverageData() {
      checkForTaskSuccess("filterCoverageData")
      checkForTheCoverageFile()
   }

   @Test
   void doesGenerateCoverageXML() {
      checkForTaskSuccess("generateLcovXml")
      checkForTheXMLFile()
   }

   @Test
   void doesGenerateCoverageDataHtml() {
      checkForTaskSuccess("generateCoverageDataHtml")
      checkForTheHtmlFile()
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
            Assert.assertTrue("The file does not exist: ${f.absolutePath}",f.exists())
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
            Assert.assertTrue("The file does not exist: ${f.absolutePath}",f.exists())
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
         }

//         def eachSubprojectDirName = "com.ngc.blocs.cpp." + eachSubprojectName
//         def subprojectDir = new File("${project.projectDir}/${eachSubprojectDirName}")
//         def subproject = ProjectBuilder.builder().withProjectDir(subprojectDir).withParent(project).build()
//         def cppCoverageExtension = new SeasideCppCoverageExtension(subproject)
//         def coverageFile = new File(cppCoverageExtension.coverageFilePath)
//         def htmlFile = new File(cppCoverageExtension.CPP_COVERAGE_PATHS.PATH_TO_THE_COVERAGE_HTML_DIR + "/index.html")
//
//         if (coverageFile.exists()) {
//            Assert.assertTrue("The file does not exist: ${htmlFile.absolutePath}", htmlFile.exists())
//         }
      }
   }

   private checkForTheXMLFile() {
      // TODO(Cameron): need to fill this in
      subprojectNames.each { subprojectName ->
         def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
         if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0]))
            return

         if (isSubproject(file)) {
            coverageExtension = createAnExtensionOnTheSubproject(file)
            def f = new File(coverageExtension.coverageXmlPath)
            Assert.assertTrue("The file does not exist: ${f.absolutePath}",f.exists())
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
