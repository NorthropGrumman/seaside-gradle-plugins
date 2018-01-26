package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class UpdateVersionTask extends DefaultTask {

    boolean dryRun
    private final VersionResolver resolver
    private ReleaseType typeOfRelease

    @Inject
    UpdateVersionTask() {
        this.resolver = new VersionResolver(project)
        this.typeOfRelease = ReleaseType.MINOR
    }

    UpdateVersionTask(VersionResolver resolver, ReleaseType typeOfRelease = ReleaseType.PATCH) {
        this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
        this.typeOfRelease = typeOfRelease
    }

    @TaskAction
    def updateReleaseVersion() {
        def newReleaseVersion = getVersionForRelease()
    }

    boolean isReleaseVersionSet() {
        return project.rootProject.hasProperty("releaseVersion")
    }

    String getVersionForRelease() {
        def upgradeStrategy = resolver.resolveVersionUpgradeStrategy(releaseType)
        return upgradeStrategy.getVersion(getCurrentVersion())
    }

    String getCurrentVersion() {
        return resolver.getProjectVersion(releaseType)
    }

    ReleaseType getReleaseType() {
        return typeOfRelease
    }
}
