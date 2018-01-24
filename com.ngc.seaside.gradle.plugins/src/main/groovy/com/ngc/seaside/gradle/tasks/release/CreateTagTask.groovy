package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Used to create the tags for builds
 * */
class CreateTagTask extends DefaultTask {

   private static final DEFAULT_TAG_PREFIX = 'v'

   private String tagName

   boolean dryRun

   boolean commitChanges

   CreateTagTask(){
   }

   @TaskAction
   def createReleaseTag() {
    createTag(commitChanges, dryRun)
   }

   /**
    *
    * @param tagPrefix
    * @param versionSuffix
    * @return
    */
   String getTagName(String tagPrefix, String versionSuffix) {
      return tagName
   }

   /**
    *
    * @param tagName
    */
   private void createTag(boolean commitChanges, boolean dryRun) {

      if (commitChanges && !dryRun) {
         git "tag", "-a", tagName, "-m Release of $tagName"
         project.logger.debug("Created release tag: $tagName")
      }

      if (dryRun) {
         project.logger.lifecycle("Dry Run >> Would have created release tag: $tagName")
      }
   }
   /**
    *
    * @param arguments
    */
   private void git(Object[] arguments) {
      project.logger.debug("Will run: git $arguments")
      def output = new ByteArrayOutputStream()

      project.exec {
         executable "git"
         args arguments
         standardOutput output
         ignoreExitValue = true
      }

      output = output.toString().trim()
      if (!output.isEmpty()) {
         project.logger.debug(output)
      }
   }
}
