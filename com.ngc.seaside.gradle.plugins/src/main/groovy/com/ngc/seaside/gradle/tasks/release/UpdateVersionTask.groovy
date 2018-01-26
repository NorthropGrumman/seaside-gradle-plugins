package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateVersionTask extends DefaultTask {

    private final VersionResolver resolver
    private ReleaseType typeOfRelease

    UpdateVersionTask() {
        this.resolver = new VersionResolver(project)
        this.typeOfRelease = ReleaseType.PATCH
    }

    UpdateVersionTask(VersionResolver resolver, ReleaseType typeOfRelease = ReleaseType.PATCH) {
        this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
        this.typeOfRelease = typeOfRelease
    }

    @TaskAction
    def updateReleaseVersion() {
        Preconditions.checkState(
              isReleaseVersionSet(),
              "Must call prepareForReleaseIfNeeded() during configuration phase."
        )
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

    private void getReleaseExtensionSettings() {}
}
