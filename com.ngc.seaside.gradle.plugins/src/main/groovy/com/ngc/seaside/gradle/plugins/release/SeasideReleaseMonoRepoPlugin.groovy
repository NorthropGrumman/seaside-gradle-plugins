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

    @Override
    void doApply(Project project) {
        releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))

        project.task(
            RELEASE_UPDATE_VERSION_TASK_NAME,
            type: UpdateVersionTask,
            group: RELEASE_MONO_REPO_TASK_GROUP_NAME,
            description: 'Define a release version (i.e. remove -SNAPSHOT) and commit it.') {}

        project.task(
            RELEASE_CREATE_TAG_TASK_NAME,
            type: CreateTagTask,
            group: RELEASE_MONO_REPO_TASK_GROUP_NAME,
            description: 'Create the version tag used by GitHub') {}
    }
}
