package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.tasks.release.ReleaseTask
import com.ngc.seaside.gradle.tasks.release.ReleaseType
import org.gradle.api.Project

class SeasideReleasePlugin extends AbstractProjectPlugin {
    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'
    private static final String DRY_RUN_TASK_NAME_SUFFIX = 'DryRun'

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
        project.logger.info(String.format("Creating release tasks for %s", project.name))
        configureReleaseTask(project,
                             RELEASE_TASK_NAME,
                             'Creates a tagged non-SNAPSHOT release.',
                             ReleaseType.PATCH,
                             false)
        configureReleaseTask(project,
                             RELEASE_TASK_NAME + DRY_RUN_TASK_NAME_SUFFIX,
                             'Performs a dry run of a non-SNAPSHOT release.',
                             ReleaseType.PATCH,
                             true)

        configureReleaseTask(project,
                             RELEASE_MAJOR_VERSION_TASK_NAME,
                             'Upgrades to next major version & creates a tagged non-SNAPSHOT release.',
                             ReleaseType.MAJOR,
                             false)
        configureReleaseTask(project,
                             RELEASE_MAJOR_VERSION_TASK_NAME + DRY_RUN_TASK_NAME_SUFFIX,
                             'Performs a dry run of an upgrade to the next major version and a non-SNAPSHOT release.',
                             ReleaseType.MAJOR,
                             true)

        configureReleaseTask(project,
                             RELEASE_MINOR_VERSION_TASK_NAME,
                             'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.',
                             ReleaseType.MINOR,
                             false)
        configureReleaseTask(project,
                             RELEASE_MINOR_VERSION_TASK_NAME + DRY_RUN_TASK_NAME_SUFFIX,
                             'Performs a dry run of an upgrade to next minor version and a non-SNAPSHOT release.',
                             ReleaseType.MINOR,
                             true)
    }

    private void configureReleaseTask(Project project,
                                      String name,
                                      String description,
                                      ReleaseType releaseType,
                                      boolean isDryRun) {
        project.afterEvaluate {
            def task = project.task(name,
                                    type: ReleaseTask,
                                    group: RELEASE_TASK_GROUP_NAME,
                                    description: description) {
                commitChanges = releaseExtension.commitChanges
                push = releaseExtension.push
                tagPrefix = releaseExtension.tagPrefix
                versionSuffix = releaseExtension.versionSuffix
                dryRun = isDryRun
                prepareForReleaseIfNeeded(releaseType)
                dependsOn {
                    project.rootProject.subprojects.collect { subproject ->
                        taskResolver.findTask(subproject, "build")
                    }
                }
            }

            if (releaseExtension.uploadArtifacts && !isDryRun) {
                task.dependsOn(taskResolver.findTask("uploadArchives"))
            }
        }
    }
}
