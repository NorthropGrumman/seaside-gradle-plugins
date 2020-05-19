/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
