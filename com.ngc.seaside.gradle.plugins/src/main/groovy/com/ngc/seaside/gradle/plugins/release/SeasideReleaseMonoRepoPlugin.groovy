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
     *
     * @param project
     */
    @Override
    void doApply(Project project) {
        releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))
        createTasks(project)
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {

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
     *
     * Attach Task to this project and group
     *
     * @param project
     * @param name
     * @param type
     * @param group
     * @param descriptiton
     */
    private void createMonoTask(Project project, String name, Class type, String group, String description) {
        project.task(name, type: type, group: group, description: description){}
    }
}
