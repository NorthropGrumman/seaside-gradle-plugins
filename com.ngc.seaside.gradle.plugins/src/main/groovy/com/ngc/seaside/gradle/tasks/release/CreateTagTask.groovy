package com.ngc.seaside.gradle.tasks.release

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

   /**
    * function required to be a task within the gradle framework
    * @return
    */
   @TaskAction
   def createReleaseTag() {

    tagName = DEFAULT_TAG_PREFIX + "$project.version"
    createTag(commitChanges, dryRun)

   }

   /**
    *
    * @param tagPrefix
    * @param versionSuffix
    * @return
    */
   String getTagName() {

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
