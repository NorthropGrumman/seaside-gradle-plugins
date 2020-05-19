/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.release.task

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.release.ReleaseType
import com.ngc.seaside.gradle.plugins.release.ReleaseUtil
import com.ngc.seaside.gradle.plugins.release.SeasideReleaseRootProjectPlugin
import com.ngc.seaside.gradle.plugins.version.VersionResolver

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * Used to create the tags for new releases to our GitHub repository
 */
class CreateTagTask extends DefaultTask {
   private String tagPrefix
   private String tagName

   //will be used in the future
   boolean dryRun

   private ReleaseType releaseType = ReleaseType.MINOR
   VersionResolver resolver

   /**
    * CTOR
    *
    */
   @Inject
   CreateTagTask() {
      resolver = new VersionResolver(project)
      resolver.enforceVersionSuffix = false
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
      setTagName()
      ReleaseUtil.getReleaseExtension(project, SeasideReleaseRootProjectPlugin.RELEASE_ROOT_PROJECT_EXTENSION_NAME).tag = tagName
      createTag()

   }

   /**
    * Set the tag name that will represent a release.
    */
   void setTagName(){
      tagName = getTagPrefix() + getCurrentVersion()
   }

   /**
    * Get the tag name.
    *
    * @return The string representation of the tagged release version.
    */
   String getTagName() {
      return tagName
   }

   /**
    *
    * @return tagPrefix usually a 'v'
    */
   String getTagPrefix(){
      return tagPrefix
   }

   /**
    * Create the tag in git.
    */
   private void createTag() {
      project.exec ReleaseUtil.git("tag", "-a", tagName, "-m Release of $tagName")
      logger.lifecycle("Created release tag: $tagName")
   }

   /**
    * Get the project's current version.
    *
    * @return String based on the current version in the version file.
    */
   private String getCurrentVersion() {
      return resolver.getProjectVersion()
   }

   /**
    * Resolves class variables with the Project extension variables
    */
   private void getReleaseExtensionSettings() {
      tagPrefix = ReleaseUtil.getReleaseExtension(project, SeasideReleaseRootProjectPlugin.RELEASE_ROOT_PROJECT_EXTENSION_NAME).getTagPrefix()
   }
}
