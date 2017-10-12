package com.ngc.seaside.gradle.tasks.bats

import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class RunBatsTask extends DefaultTask {
   SeasideBatsExtension batsExtension =
            project.extensions
                   .findByType(SeasideBatsExtension.class)

   @TaskAction
   runBats() {
      def bats = pathToTheBatsScript()
      def tests = enumerateAllBatsFilesIn(pathToTheDirectoryWithBatsTests())
      def commandOutput = new ByteArrayOutputStream()

      project.exec {
         executable bats
         args tests
         standardOutput commandOutput
      }

      print commandOutput.toString()
      writeTestResultsFile(commandOutput)
   }

   private String pathToTheBatsScript() {
      return batsExtension.BATS_PATHS.PATH_TO_THE_BATS_SCRIPT
   }

   private String pathToTheDirectoryWithBatsTests() {
      return batsExtension.batsTestsDir
   }

   private Set<File> enumerateAllBatsFilesIn(String path) {
      return project.fileTree(path).getFiles()
                    .findAll { file -> file.name.endsWith(".bats") }
   }

   private void writeTestResultsFile(ByteArrayOutputStream commandOutput) {
      def f = resultsFile(pathToTheBatsResultsFile())
      f << commandOutput.toString()
   }

   private File resultsFile(String path) {
      return createTheResultsFileIfNecessary(project.file(path))
   }

   private static File createTheResultsFileIfNecessary(File f) {
      f.getParentFile().mkdirs()
      f.createNewFile()
      return f
   }

   private String pathToTheBatsResultsFile() {
      return batsExtension.resultsFile
   }
}
