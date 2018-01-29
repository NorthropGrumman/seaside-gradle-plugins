package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * Used to create the tags for new releases to our GitHub repository
 * */
class CreateTagTask extends DefaultTask {


   private String tagPrefix
   private String tagName

   //will be used in the future
   boolean dryRun
   boolean commitChanges

   private ReleaseType releaseType = ReleaseType.MINOR
   VersionResolver resolver

   /**
    * CTOR
    *
    */
   @Inject
   CreateTagTask(){
      resolver = new VersionResolver(project)
   }

   /**
    * CTOR used by testing framework
    * @param resolver
    * @param typeOfRelease defaulted to ReleaseType Minor
    * @param version prefix
    */
   CreateTagTask(VersionResolver resolver,
                 ReleaseType typeOfRelease = ReleaseType.MINOR,
                 String prefix) {
      this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
      this.releaseType = typeOfRelease
      this.tagPrefix = prefix
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
      tagName = setTagName()
      ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).tag = tagName
      createTag(commitChanges, dryRun)

   }

   /**
    *
    * @return tag that will be used for a release
    */
   String setTagName(){
      return tagName = getTagPrefix() + getCurrentVersion()
   }

   /**
    *
    * @return The string of the version of the tag that was
    *    pushed to gitHub for this release
    */
   String getTagName() {

      return tagName

   }

   String getTagPrefix(){
      return tagPrefix
   }

   /**
    *
    * @param commit Changes to really push the changes or not
    * @param dryRun Not actually creating the tag
    */
   private void createTag(boolean commitChanges, boolean dryRun) {
      project.exec ReleaseUtil.git("tag", "-a", tagName, "-m Release of $tagName")
      logger.debug("Created release tag: $tagName")
   }

   /**
    *
    * @return String based on the current version in the build.gradle file
    */
   private String getCurrentVersion() {
      return resolver.getProjectVersion()
   }

   /**
    * Resolves class variables with the Project extension variables
    */
   private void getReleaseExtensionSettings() {
      commitChanges = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).getCommitChanges()
      tagPrefix = ReleaseUtil.getReleaseExtension(project, SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME).getTagPrefix()
   }
}
