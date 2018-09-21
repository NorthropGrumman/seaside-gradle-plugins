/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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
