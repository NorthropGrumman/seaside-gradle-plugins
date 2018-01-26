package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.ReleaseUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Used to create the tags for builds
 * */
class CreateTagTask extends DefaultTask {


   private String tagPrefix
   private String tagName

   boolean dryRun

   boolean commitChanges

   CreateTagTask(){
   }

   def prepareForReleaseIfNeeded(ReleaseType releaseType) {
     project.gradle.startParameter.taskNames.contains(name)
   }

   /**
    * function required to be a task within the gradle framework
    * @return
    */
   @TaskAction
   def createReleaseTag() {
//      Preconditions.checkState(
//        ReleaseUtil.isExtensionSet(project),
//        "createReleaseTag task executing but prepareForReleaseIfNeeded() not invoked during configuration phase!")
      getReleaseExtensionSettings()
      tagName = tagPrefix + "$project.version"
      ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).tag = tagName
      createTag(commitChanges, dryRun)

   }

   /**
    *
    * @return The string of the version of the tag that was
    *    pushed to gitHub for this release
    */
   String getTagName() {

      return tagName

   }

   /**
    *
    * @param commit Changes to really push the changes or not
    * @param dryRun Not actually creating the tag
    */
   private void createTag(boolean commitChanges, boolean dryRun) {

      if (commitChanges && !dryRun) {
         git "tag", "-a", tagName, "-m Release of $tagName"
         logger.debug("Created release tag: $tagName")
      }

      if (dryRun) {
         logger.lifecycle("Dry Run >> Would have created release tag: $tagName")
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

   private void getReleaseExtensionSettings() {
      commitChanges = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).commitChanges
      tagPrefix = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).tagPrefix
   }
}
