package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.plugins.util.TaskResolver
import com.ngc.seaside.gradle.tasks.release.ReleaseTask
import org.gradle.api.Project

class SeasideReleasePlugin extends AbstractProjectPlugin {

    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'

    private SeasideReleaseExtension releaseExtension

    String uploadArtifacts
    String push
    String tagPrefix
    String versionSuffix
    String commitChanges
    String dryRun = "false"


    @Override
    void doApply(Project project) {
        project.configure(project) {
            releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
            project.logger.info(String.format("Initializing extensions for %s", project.name))

            // This has to be done in this closure else, the scope is lost
            if (Boolean.parseBoolean(dryRun)) {
                releaseExtension.push = false
                releaseExtension.commitChanges = false
                releaseExtension.uploadArtifacts = false
            } else {
                // Pass properties set with -D or -P to override the extension
                releaseExtension.uploadArtifacts = (uploadArtifacts) ? Boolean.parseBoolean(uploadArtifacts) :
                                                   releaseExtension.uploadArtifacts
                releaseExtension.push = (push) ? Boolean.parseBoolean(push) : releaseExtension.push
                releaseExtension.tagPrefix = (tagPrefix) ? tagPrefix : releaseExtension.tagPrefix
                releaseExtension.versionSuffix = (versionSuffix) ? versionSuffix : releaseExtension.versionSuffix
                releaseExtension.commitChanges = (commitChanges) ? Boolean.parseBoolean(commitChanges) :
                                                 releaseExtension.commitChanges
            }

            createTasks(project)

            project.afterEvaluate {
                TaskResolver.findTask(project, RELEASE_TASK_NAME) {
                    if (releaseExtension.uploadArtifacts) {
                        finalizedBy {
                            taskResolver.findTask("uploadArchives")
                        }
                    }
                }

                TaskResolver.findTask(project, RELEASE_MINOR_VERSION_TASK_NAME) {
                    if (releaseExtension.uploadArtifacts) {
                        finalizedBy {
                            taskResolver.findTask("uploadArchives")
                        }
                    }
                }

                TaskResolver.findTask(project, RELEASE_MINOR_VERSION_TASK_NAME) {
                    if (releaseExtension.uploadArtifacts) {
                        finalizedBy {
                            taskResolver.findTask("uploadArchives")
                        }
                    }
                }
            }
        }
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
        project.logger.info(String.format("Creating tasks for %s", project.name))
        project.task(RELEASE_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                     description: 'Creates a tagged non-SNAPSHOT release.') {
            dependsOn {
                project.rootProject.subprojects.collect { subproject ->
                    taskResolver.findTask(subproject, "build")
                }
            }
        }

        project.task(RELEASE_MAJOR_VERSION_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                     description: 'Upgrades to next major version & creates a tagged non-SNAPSHOT release.') {
            dependsOn {
                project.rootProject.subprojects.collect { subproject ->
                    taskResolver.findTask(subproject, "build")
                }
            }
        }

        project.task(RELEASE_MINOR_VERSION_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                     description: 'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.') {
            dependsOn {
                project.rootProject.subprojects.collect { subproject ->
                    taskResolver.findTask(subproject, "build")
                }
            }
        }
    }
}

