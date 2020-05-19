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
