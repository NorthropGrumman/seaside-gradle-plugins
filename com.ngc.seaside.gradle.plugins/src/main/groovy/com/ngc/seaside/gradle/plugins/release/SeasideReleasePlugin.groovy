package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.IVersionUpgradeStrategy
import com.ngc.seaside.gradle.tasks.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.tasks.release.SeasideReleaseTask
import com.ngc.seaside.gradle.tasks.release.VersionUpgradeStrategyFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by J57467 on 9/10/2017.
 */
class SeasideReleasePlugin implements Plugin<Project> {

    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'
    String uploadArtifacts
    String push
    String tagPrefix
    String versionSuffix

    @Override
    void apply(Project p) {
        p.configure(p) {
            def releaseExtension = p.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension, p)
            releaseExtension.uploadArtifacts = (uploadArtifacts)? Boolean.parseBoolean(uploadArtifacts) : releaseExtension.uploadArtifacts
            releaseExtension.push = (push)? Boolean.parseBoolean(push) : releaseExtension.uploadArtifacts
            releaseExtension.tagPrefix = (tagPrefix)? tagPrefix : releaseExtension.tagPrefix
            releaseExtension.versionSuffix = (versionSuffix)? versionSuffix : releaseExtension.versionSuffix

            // Define the tasks
            task(RELEASE_TASK_NAME, type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }

            task(RELEASE_MAJOR_VERSION_TASK_NAME, type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next major version & creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }

            task(RELEASE_MINOR_VERSION_TASK_NAME, type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.') {
                dependsOn subprojects*.build
                if (releaseExtension.uploadArtifacts) {
                    finalizedBy {
                        uploadArchives
                    }
                }
            }

            // Get project release version and prepare build project/file for release. Do this once
            if (!p.rootProject.hasProperty("releaseVersion")) {
                def versionFromFile = releaseExtension.getPreReleaseVersionFromFile()
                def taskNames = p.gradle.startParameter.taskNames

                IVersionUpgradeStrategy upgradeStrategy =
                        resolveVersionUpgradeStrategy(taskNames, releaseExtension.versionSuffix)
                def releaseVersion = upgradeStrategy.getVersion(versionFromFile)
                p.getLogger().debug("Using release version '$releaseVersion'")

                if (!p.gradle.startParameter.dryRun && (versionFromFile != releaseVersion)) {
                    p.logger.debug(
                            "Writing release version '$releaseVersion' to file '$releaseExtension.versionFile'")
                    releaseExtension.setVersionOnFile(releaseVersion)
                }
                println "**************************************************"
                println("Setting project version to '$releaseVersion'")
                p.rootProject.ext.set("releaseVersion", releaseVersion)
            }

            // Set the extension for the subprojects
            releaseExtension.setReleaseVersion(p.rootProject.releaseVersion)
            p.version = p.rootProject.releaseVersion
        }
    }
/**
 * Resolves Semantic versioning upgrade strategy based on the release task called by the user
 * @param taskNames all gradle tasks that are called by the user
 * @param versionSuffix project version suffix
 * @return {@link com.ngc.seaside.gradle.tasks.release.IVersionUpgradeStrategy}
 */
    private static IVersionUpgradeStrategy resolveVersionUpgradeStrategy(List<String> taskNames, String versionSuffix) {
        if (taskNames.contains(RELEASE_MAJOR_VERSION_TASK_NAME)) {
            return VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(versionSuffix)
        } else if (taskNames.contains(RELEASE_MINOR_VERSION_TASK_NAME)) {
            return VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(versionSuffix)
        } else if (taskNames.contains(RELEASE_TASK_NAME)) {
            return VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(versionSuffix)
        } else {
            return VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy()
        }
    }
}