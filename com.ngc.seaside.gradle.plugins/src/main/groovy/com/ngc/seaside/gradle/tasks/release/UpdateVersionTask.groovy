package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateVersionTask extends DefaultTask {
    private ReleaseType typeOfRelease

    @TaskAction
    def updateReleaseVersion() {}

    void prepareForSnapshotRelease() {
        typeOfRelease = ReleaseType.SNAPSHOT
    }

    ReleaseType getReleaseType() {
        return typeOfRelease
    }
}
