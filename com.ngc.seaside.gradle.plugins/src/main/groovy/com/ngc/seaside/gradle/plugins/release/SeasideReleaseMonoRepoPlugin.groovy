package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.tasks.release.CreateTagTask
import org.gradle.api.Project

class SeasideReleaseMonoRepoPlugin extends AbstractProjectPlugin {

    public static final String RELEASE_MONO_REPO_TASK_GROUP_NAME = 'ReleaseMono'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'
    public static final String CREATE_TAG_TASK = "CreateTagTask"

    private SeasideReleaseExtension releaseExtension

    @Override
    void doApply(Project project) {

        project.configure(project) {
            releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
            project.logger.info(String.format("Initializing release extensions for %s", project.name))
            createTasks(project)
        }
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
//        configureReleaseTask(project, CREATE_TAG_TASK, "Will create the version tag used by gitHub")
        project.task(CREATE_TAG_TASK, type: com.ngc.seaside.gradle.tasks.release.CreateTagTask)//,
                //group: RELEASE_MONO_REPO_TASK_GROUP_NAME,
                //description: "Will create the version tag used by gitHub")

    }

    private void configureReleaseTask(Project project,
                                      String name,
                                      String description
                                      ) {
        project.afterEvaluate {
            def task = project.task(name,
                    type: CreateTagTask,
                    group: RELEASE_MONO_REPO_TASK_GROUP_NAME,
                    description: description)

//            if (releaseExtension.uploadArtifacts ) {
//                task.dependsOn(taskResolver.findTask("uploadArchives"))
//            }
        }
    }
}
