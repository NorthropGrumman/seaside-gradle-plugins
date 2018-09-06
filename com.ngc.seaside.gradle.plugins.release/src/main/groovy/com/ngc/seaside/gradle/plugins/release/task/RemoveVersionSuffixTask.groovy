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
import com.ngc.seaside.gradle.plugins.release.ReleaseUtil
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.util.Versions

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject


/**
 * Updates the version of the project to prepare for a release.
 */
class RemoveVersionSuffixTask extends DefaultTask {
    private final VersionResolver resolver

    /**
     * CTOR
     *
     * @Inject was used because there are two constructors and gradle seems to be confused on which constructor to use.
     */
    @Inject
    RemoveVersionSuffixTask() {
        this.resolver = new VersionResolver(project)
    }

    /**
     * CTOR used by testing framework
     *
     * @param resolver An instance of a version resolver for the current projecet.
     * @param typeOfRelease The type of release to be performed (default: ReleaseType.MINOR)
     */
    RemoveVersionSuffixTask(VersionResolver resolver) {
        this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
    }

    /**
     * Function that defines what the task actually does. This function is actually the entry point for the task when
     * Gradle runs it.
     */
    @TaskAction
    void removeVersionSuffix() {
        def versionForRelease = getVersionForRelease()
        resolver.setProjectVersionOnFile(versionForRelease)
        project.exec ReleaseUtil.git("commit", "-m", "Release of version v$versionForRelease", resolver.versionFile.absolutePath)
    }

    /**
     * Get the version that will be released - specifically, the current version without the version suffix.
     *
     * @return version used for the current release
     */
    String getVersionForRelease() {
        return getCurrentVersion() - Versions.VERSION_SUFFIX
    }

    /**
     * Get the project's current version.
     *
     * @return version from the current version file
     */
    String getCurrentVersion() {
        return resolver.getProjectVersion()
    }

}
