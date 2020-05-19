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
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.util.Versions

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * Used to increment the version in the version file and then commit it to the repository.
 */
class BumpVersionTask extends DefaultTask {
   private ReleaseType releaseType = ReleaseType.MINOR
   private VersionResolver resolver

   /**
    * CTOR
    *
    * @Inject was used because there are two constructors and gradle seems to be confused on which constructor to use.
    */
   @Inject
   BumpVersionTask() {
      resolver = new VersionResolver(project)
      resolver.enforceVersionSuffix = false
   }

   /**
    * CTOR used by testing framework
    *
    * @param resolver
    * @param typeOfRelease defaulted to ReleaseType Minor
    */
   BumpVersionTask(VersionResolver resolver, ReleaseType typeOfRelease = ReleaseType.MINOR) {
      this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
      this.releaseType = typeOfRelease
   }

   /**
    * Function that defines what the task actually does. This function is actually the entry point for the task when
    * Gradle runs it.
    */
   @TaskAction
   void bumpTheVersion() {
      def nextVersion = getVersionAfterRelease()
      resolver.setProjectVersionOnFile(nextVersion)
      project.exec ReleaseUtil.git("commit", "-m", "Creating new $nextVersion version after release", resolver.versionFile.absolutePath)
      logger.lifecycle("\n Updated project version to $nextVersion")
   }

   /**
    * Get the next beta release version to be used for the project after applying the specified type of release.
    *
    * @return The next beta release version.
    */
   String getVersionAfterRelease() {
      return getUpdatedVersion() + Versions.VERSION_SUFFIX
   }

   /**
    * Gets the type of release (i.e. major, minor, patch, snapshot) to be used for setting the updated version.
    *
    * @return The type of release to be performed.
    */
   ReleaseType getReleaseType() {
      return releaseType
   }

   /**
    * Sets the type of release (i.e. major, minor, patch, snapshot) to be used for setting the updated version.
    *
    * @param type the type of release (i.e. major, minor, patch, snapshot) to be used for setting the updated version
    */
   void setReleaseType(ReleaseType type) {
      this.releaseType = Preconditions.checkNotNull(type, "type may not be null!")
   }

   /**
    * Get the updated version after applying the specified release type.
    *
    * @return The project version after applying the specified release type.
    */
   private String getUpdatedVersion() {
      return resolver.getUpdatedProjectVersionForRelease(releaseType)
   }

   /**
    * Get the current version of this project.
    *
    * @return The current project version.
    */
   String getCurrentVersion() {
      return resolver.getProjectVersion()
   }
}
