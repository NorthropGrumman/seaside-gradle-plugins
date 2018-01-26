package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject


/**
 * updates the version for gradle builds
 */
class UpdateVersionTask extends DefaultTask {

    //will be used in the future
    boolean dryRun

    private final VersionResolver resolver
    private ReleaseType typeOfRelease

    /**
     * CTOR
     *
     * @Inject was used because there are two constructors and gradle seems to
     *    be confused on which constructor to use
     */
    @Inject
    UpdateVersionTask() {
        this.resolver = new VersionResolver(project)

        //This will be default for now
        this.typeOfRelease = ReleaseType.MINOR
    }

    /**
     * CTOR used by testing framework
     * @param resolver
     * @param typeOfRelease defaulted to ReleaseType PATCH
     */
    UpdateVersionTask(VersionResolver resolver, ReleaseType typeOfRelease = ReleaseType.PATCH) {
        this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
        this.typeOfRelease = typeOfRelease
    }

    /**
     * function required to be a task within the
     * gradle framework and is the entry point for
     * gradle
     *
     * @return
     */
    @TaskAction
    def updateReleaseVersion() {
        def newReleaseVersion = getVersionForRelease()
    }

    /**
     *
     * @return version used for the next release
     */
    String getVersionForRelease() {
        def upgradeStrategy = resolver.resolveVersionUpgradeStrategy(releaseType)
        return upgradeStrategy.getVersion(getCurrentVersion())
    }

    /**
     *
     * @return version from the current build.gradle file
     */
    String getCurrentVersion() {
        return resolver.getProjectVersion(releaseType)
    }

    /**
     *
     * @return release type for current release
     */
    ReleaseType getReleaseType() {
        return typeOfRelease
    }
}
