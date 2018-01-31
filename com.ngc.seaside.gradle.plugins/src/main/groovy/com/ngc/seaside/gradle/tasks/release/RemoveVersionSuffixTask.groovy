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
        println(">>>>>>>>>>>>>>>> CVC: are we even getting here?")
        resolver.setVersionFile(project.rootProject.rootDir)
        def versionForRelease = getVersionForRelease()
        resolver.setProjectVersionOnFile(versionForRelease)
        project.exec ReleaseUtil.git("commit", "-m", "Releasing of version v$versionForRelease", resolver.versionFile.absolutePath)
    }

    /**
     * Get the version that will be released - specifically, the current version without the version suffix.
     *
     * @return version used for the current release
     */
    String getVersionForRelease() {
        return getCurrentVersion() - resolver.VERSION_SUFFIX
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
