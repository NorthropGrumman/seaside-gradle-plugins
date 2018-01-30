package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject


/**
 * Updates the version of the project to prepare for a release.
 */
class UpdateVersionTask extends DefaultTask {
    //will be used in the future
    boolean dryRun

    private final VersionResolver resolver

    /**
     * CTOR
     *
     * @Inject was used because there are two constructors and gradle seems to be confused on which constructor to use.
     */
    @Inject
    UpdateVersionTask() {
        this.resolver = new VersionResolver(project)
    }

    /**
     * CTOR used by testing framework
     *
     * @param resolver An instance of a version resolver for the current projecet.
     * @param typeOfRelease The type of release to be performed (default: ReleaseType.MINOR)
     */
    UpdateVersionTask(VersionResolver resolver) {
        this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
    }

    /**
     * Function that defines what the task actually does. This function is actually the entry point for the task when
     * Gradle runs it.
     */
    @TaskAction
    void updateReleaseVersion() {
        resolver.enforceVersionSuffix = false
        def newReleaseVersion = getVersionForRelease()
        resolver.setProjectVersionOnFile(newReleaseVersion)
        project.exec ReleaseUtil.git("commit", "-m", "Releasing Version $newReleaseVersion : $resolver.versionFile.absolutePath")
    }

    /**
     * Get the next version that will be released based on the current version and the type of release that will be
     * performed.
     *
     * @return version used for the next release
     */
    String getVersionForRelease() {
        return resolver.getProjectVersion()
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
