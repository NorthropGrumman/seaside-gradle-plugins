package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.tasks.release.UpdateVersionTask
import com.ngc.seaside.gradle.tasks.release.CreateTagTask
import org.gradle.api.Project

class SeasideReleaseMonoRepoPlugin extends AbstractProjectPlugin {
    public static final String RELEASE_MONO_REPO_TASK_GROUP_NAME = 'Mono Repo Release'
    public static final String RELEASE_EXTENSION_NAME = 'seasideReleaseMonoRepo'
    public static final String RELEASE_UPDATE_VERSION_TASK_NAME = 'updateReleaseVersion'
    public static final String RELEASE_CREATE_TAG_TASK_NAME = 'createReleaseTag'

    private SeasideReleaseExtension releaseExtension

    /**
     * Perform the necessary actions for applying the plugin.
     *
     * @param project The project on which the plugin should be applied.
     */
    @Override
    void doApply(Project project) {
        releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))
        createTasks(project)
    }

    /**
     * Create project tasks for this plugin
     *
     * @param project The project for which the tasks are to be created.
     */
    private static void createTasks(Project project) {
        createMonoTask(project,
            RELEASE_UPDATE_VERSION_TASK_NAME,
            UpdateVersionTask,
            RELEASE_MONO_REPO_TASK_GROUP_NAME,
            'Define a release version (i.e. remove -SNAPSHOT) and commit it.')

        createMonoTask(project,
            RELEASE_CREATE_TAG_TASK_NAME,
            CreateTagTask,
            RELEASE_MONO_REPO_TASK_GROUP_NAME,
            'Create the version tag used by GitHub')
    }

    /**
     * Attach Task to this project and group
     *
     * @param project The project to which the task should be attached.
     * @param name The name of the task.
     * @param type The type of task being added.
     * @param group The task group to which the task belongs.
     * @param descriptiton A brief description of what the task does.
     */
    private static void createMonoTask(Project project, String name, Class type, String group, String description) {
        project.task(
                name,
                type: type,
                group: group,
                description: description)
    }
}
