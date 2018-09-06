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
