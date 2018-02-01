package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.tasks.release.BumpVersionTask
import com.ngc.seaside.gradle.tasks.release.ReleasePushTask
import com.ngc.seaside.gradle.tasks.release.RemoveVersionSuffixTask
import com.ngc.seaside.gradle.tasks.release.CreateTagTask
import com.ngc.seaside.gradle.util.ReleaseUtil
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
              'Will bump the version (i.e. add -SNAPSHOT) in the version file.')

        ReleaseUtil.configureTask(
              project,
              ReleasePushTask,
              RELEASE_ROOT_PROJECT_TASK_GROUP_NAME,
              RELEASE_PUSH_TASK_NAME,
              'Push the project to GitHub.')
    }
}
