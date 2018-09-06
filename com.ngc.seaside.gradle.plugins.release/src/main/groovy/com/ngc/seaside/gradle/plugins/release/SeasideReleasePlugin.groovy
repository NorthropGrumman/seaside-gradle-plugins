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
package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.release.task.ReleaseTask
import com.ngc.seaside.gradle.plugins.version.SeasideVersionPlugin
import com.ngc.seaside.gradle.util.TaskResolver

import org.gradle.api.Project

class SeasideReleasePlugin extends AbstractProjectPlugin {
    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'

    private SeasideReleaseExtension releaseExtension

    @Override
    void doApply(Project project) {
        project.plugins.apply(SeasideVersionPlugin)
        releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))
        createTasks(project)
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
        project.logger.info(String.format("Creating release tasks for %s", project.name))

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_TASK_NAME,
            'Creates a tagged non-SNAPSHOT release.',
            ReleaseType.PATCH,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of a non-SNAPSHOT release.',
            ReleaseType.PATCH,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_MINOR_VERSION_TASK_NAME,
            'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.',
            ReleaseType.MINOR,
            releaseExtension)

       configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
                RELEASE_MINOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of an upgrade to next minor version and a non-SNAPSHOT release.',
            ReleaseType.MINOR,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_MAJOR_VERSION_TASK_NAME,
            'Upgrades to next major version & creates a tagged non-SNAPSHOT release.',
            ReleaseType.MAJOR,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
                RELEASE_MAJOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of an upgrade to the next major version and a non-SNAPSHOT release.',
            ReleaseType.MAJOR,
            releaseExtension)


    }

    private static void configureTask(Project project,
                              Class type,
                              String group,
                              String name,
                              String description,
                              ReleaseType releaseType,
                              SeasideReleaseExtension releaseExtension) {
        boolean isDryRun = name.endsWith(ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX)
        project.afterEvaluate {
            def task = project.task(name,
                    type: type,
                    group: group,
                    description: description) {
                dryRun = isDryRun
                prepareForReleaseIfNeeded(releaseType)
                dependsOn {
                    project.rootProject.subprojects.collect { subproject ->
                        TaskResolver.findTask(subproject, "build")
                    }
                }
            }

            if (releaseExtension.uploadArtifacts && !isDryRun) {
                task.dependsOn(TaskResolver.findTask(project,"uploadArchives"))
            }
        }
    }
}
