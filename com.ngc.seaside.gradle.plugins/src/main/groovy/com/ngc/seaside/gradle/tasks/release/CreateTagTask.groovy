package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.ReleaseUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Used to create the tags for new releases to our GitHub repository
 * */
class CreateTagTask extends DefaultTask {


   private String tagPrefix
   private String tagName

   //will be used in the future
   boolean dryRun

   boolean commitChanges

   CreateTagTask(){
   }


   /**
    * function required to be a task within the
    * gradle framework and is the entry point for
    * gradle
    *
    * @return
    */
   @TaskAction
   def createReleaseTag() {
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

      ReleaseUtil.git(project, "tag", "-a", tagName, "-m Release of $tagName")
      logger.debug("Created release tag: $tagName")
   }

   /**
    * Resolves class variables with the Project extension variables
    */
   private void getReleaseExtensionSettings() {
      commitChanges = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).commitChanges
      tagPrefix = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).tagPrefix
   }
}
