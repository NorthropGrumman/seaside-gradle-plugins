package com.ngc.seaside.gradle.plugins.release.task

import com.ngc.seaside.gradle.plugins.release.ReleaseUtil

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Pushes the released version to GitHub with the latest created tag
 */
class ReleasePushTask extends DefaultTask {
    boolean dryRun = false

    /**
     * CTOR
     */
    ReleasePushTask() {}

    /**
     * Function that defines what the task actually does. This function is actually the entry point for the task when
     * Gradle runs it.
     */
    @TaskAction
    def releasePush() {
        if (dryRun) {
            project.logger.lifecycle("Would have pushed release")
            project.exec ReleaseUtil.git("push", "--tags", "--dry-run", "origin", "HEAD")
            project.exec ReleaseUtil.git("reset", "--hard")
        } else {
            project.logger.lifecycle("Pushing release")
            project.exec ReleaseUtil.git("push", "--tags", "origin", "HEAD")
        }
    }
}
