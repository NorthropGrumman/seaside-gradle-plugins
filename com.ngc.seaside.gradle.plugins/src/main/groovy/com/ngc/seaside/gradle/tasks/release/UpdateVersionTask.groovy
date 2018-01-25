package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.ProjectUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpdateVersionTask extends DefaultTask {

    boolean dryRun

    UpdateVersionTask() {}

    def prepareForReleaseIfNeeded(ReleaseType releaseType) {
        project.gradle.startParameter.taskNames.contains(name)
    }

    /**
     * function required to be a task within the
     * gradle framework and is the entry point for
     * gradle
     *
     * @return
     */
    @TaskAction
    def bumpTheVersion() {
//        Preconditions.checkState(
//                ProjectUtil.isExtensionSet(project),
//                "Release task executing but prepareForReleaseIfNeeded() not invoked during configuration phase!")
//        getReleaseExtensionSettings()
    }

    private void getReleaseExtensionSettings() {}
}
