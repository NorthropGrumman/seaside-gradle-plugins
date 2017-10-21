package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.plugins.util.VersionResolver
import com.ngc.seaside.gradle.tasks.release.ReleaseTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class SeasideReleasePlugin implements Plugin<Project> {
    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'

    private String versionFromFile
    String uploadArtifacts
    String commitChanges

   String push
   String tagPrefix
   String versionSuffix
    @Override
    void apply(Project project) {
        project.configure(project) {
            def releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
            releaseExtension.uploadArtifacts = (uploadArtifacts)? Boolean.parseBoolean(uploadArtifacts) : releaseExtension.uploadArtifacts
            releaseExtension.push = (push)? Boolean.parseBoolean(push) : releaseExtension.uploadArtifacts
            releaseExtension.tagPrefix = (tagPrefix)? tagPrefix : releaseExtension.tagPrefix
            releaseExtension.versionSuffix = (versionSuffix)? versionSuffix : releaseExtension.versionSuffix
            releaseExtension.commitChanges = (commitChanges)? Boolean.parseBoolean(commitChanges) : releaseExtension.uploadArtifacts

            def taskNames = project.gradle.startParameter.taskNames
            def isReleaseJob =
               taskNames.contains(RELEASE_TASK_NAME) ||
               taskNames.contains(RELEASE_MAJOR_VERSION_TASK_NAME) ||
               taskNames.contains(RELEASE_MINOR_VERSION_TASK_NAME)
           versionFromFile = (new VersionResolver(project)).getProjectVersion(isReleaseJob)
           project.version = ReleaseTask.resolveVersionUpgradeStrategy(taskNames, releaseExtension.versionSuffix).getVersion(versionFromFile)

            task(RELEASE_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                tagPrefix = releaseExtension.tagPrefix
                versionSuffix = releaseExtension.versionSuffix
                push = releaseExtension.push
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }

            task(RELEASE_MAJOR_VERSION_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next major version & creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                tagPrefix = releaseExtension.tagPrefix
                versionSuffix = releaseExtension.versionSuffix
                push = releaseExtension.push
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }

            task(RELEASE_MINOR_VERSION_TASK_NAME, type: ReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                tagPrefix = releaseExtension.tagPrefix
                versionSuffix = releaseExtension.versionSuffix
                push = releaseExtension.push
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }
        }
    }

   String getVersionFromFile() {
      return versionFromFile
   }
}
