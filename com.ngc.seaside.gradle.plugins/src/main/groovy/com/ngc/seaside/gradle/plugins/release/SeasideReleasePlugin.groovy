package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.SeasideReleaseTask
import com.ngc.seaside.gradle.tasks.release.VersionUpgradeStrategy
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
    @Override
    void apply(Project p) {
        p.configure(p) {

            task('release', type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Creates a tagged non-SNAPSHOT release.') {}

            task('releaseMajorVersion', type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next major version & creates a tagged non-SNAPSHOT release.') {}

            task('releaseMinorVersion', type: SeasideReleaseTask, group: RELEASE_TASK_GROUP_NAME,
                 description: 'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.') {}

            def versionFromFile = releaseExtension.versionFile.text.trim()
            def taskNames = project.gradle.startParameter.taskNames

            VersionUpgradeStrategy upgradeStrategy = resolveVersionUpgradeStrategy(taskNames, releaseExtension.versionSuffix)
            def releaseVersion = upgradeStrategy.getVersion(versionFromFile)
            p.logger.debug("Using release version '$releaseVersion'")
            if (!project.gradle.startParameter.dryRun && (versionFromFile != releaseVersion)) {
                p.logger.debug("Writing release version '$releaseVersion' to file '$releaseExtension.versionFile'")
                releaseExtension.versionFile.text = releaseVersion
            }
            p.logger.debug("Setting project version to release version '$releaseVersion'")
            project.version = releaseVersion

            defaultTasks = ['build']
        }

    }

    private static VersionUpgradeStrategy resolveVersionUpgradeStrategy(List<String> taskNames, String versionSuffix) {
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