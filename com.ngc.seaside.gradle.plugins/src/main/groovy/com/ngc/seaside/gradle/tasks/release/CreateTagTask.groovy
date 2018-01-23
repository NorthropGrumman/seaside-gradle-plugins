package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.Project;

/**
 * Used to create the tags for builds
 * */
public class CreateTagTask extends DefaultTask {

   private static final DEFAULT_TAG_PREFIX = 'v'

   private Project project
   private String tagName

   String versionSuffix


   /**
    *
    * @param currentProject
    */
   CreateTagTask(Project currentProject) {
      project = currentProject
      tagName = DEFAULT_TAG_PREFIX + "$project.version" - versionSuffix
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
   void createReleaseTag() {
      tag
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
