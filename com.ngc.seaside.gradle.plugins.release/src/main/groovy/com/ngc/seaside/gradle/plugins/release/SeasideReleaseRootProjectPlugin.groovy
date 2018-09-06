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
import com.ngc.seaside.gradle.plugins.release.task.BumpVersionTask
import com.ngc.seaside.gradle.plugins.release.task.CreateTagTask
import com.ngc.seaside.gradle.plugins.release.task.ReleasePushTask
import com.ngc.seaside.gradle.plugins.release.task.RemoveVersionSuffixTask
import com.ngc.seaside.gradle.plugins.version.SeasideVersionPlugin

import org.gradle.api.Project

class SeasideReleaseRootProjectPlugin extends AbstractProjectPlugin {
    public static final String RELEASE_ROOT_PROJECT_TASK_GROUP_NAME = 'Root Project Release'
    public static final String RELEASE_ROOT_PROJECT_EXTENSION_NAME = 'seasideReleaseRoot'
    public static final String RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME = 'removeVersionSuffix'
    public static final String RELEASE_CREATE_TAG_TASK_NAME = 'createReleaseTag'
    public static final String RELEASE_BUMP_VERSION_TASK_NAME = 'bumpTheVersion'
    public static final String RELEASE_PUSH_TASK_NAME = 'releasePush'

    private SeasideReleaseExtension releaseExtension

    /**
     * Perform the necessary actions for applying the plugin.
     *
     * @param project The project on which the plugin should be applied.
     */
    @Override
    void doApply(Project project) {
        project.plugins.apply(SeasideVersionPlugin)
        releaseExtension = project.extensions.create(RELEASE_ROOT_PROJECT_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))
        createTasks(project)
    }

    /**
     * Create project tasks for this plugin
     *
     * @param project The project for which the tasks are to be created.
     */
    private static void createTasks(Project project) {
        ReleaseUtil.configureTask(
              project,
              RemoveVersionSuffixTask,
              RELEASE_ROOT_PROJECT_TASK_GROUP_NAME,
              RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME,
              'Define a release version (i.e. remove -SNAPSHOT) and commit it.')

        ReleaseUtil.configureTask(
              project,
              CreateTagTask,
              RELEASE_ROOT_PROJECT_TASK_GROUP_NAME,
              RELEASE_CREATE_TAG_TASK_NAME,
              'Create the version tag used by GitHub.')

        ReleaseUtil.configureTask(
              project,
              BumpVersionTask,
              RELEASE_ROOT_PROJECT_TASK_GROUP_NAME,
              RELEASE_BUMP_VERSION_TASK_NAME,
              'Bump the version (i.e. add -SNAPSHOT) in the version file.')

        ReleaseUtil.configureTask(
              project,
              ReleasePushTask,
              RELEASE_ROOT_PROJECT_TASK_GROUP_NAME,
              RELEASE_PUSH_TASK_NAME,
              'Push the project to GitHub.')
    }
}
