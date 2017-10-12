package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ReleaseTask extends DefaultTask {
   public static final String RELEASE_TASK_NAME = 'release'
   public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
   public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'

   @Input
   String tagPrefix

   @Input
   String versionSuffix

   @Input
   boolean push

    @TaskAction
    def release() {
       VersionResolver resolver = new VersionResolver(project)
        // Get project release version and prepare build project/file for release. Do this once
        if (!project.rootProject.hasProperty("releaseVersion")) {
            def versionFromFile = resolver.getProjectVersion(true)
            def taskNames = project.gradle.startParameter.taskNames

            IVersionUpgradeStrategy upgradeStrategy =
                     resolveVersionUpgradeStrategy(taskNames, versionSuffix)
            String releaseVersion = upgradeStrategy.getVersion(versionFromFile)
            project.getLogger().debug("Using release version '$releaseVersion'")

            if (!project.gradle.startParameter.dryRun && (versionFromFile != releaseVersion)) {
                project.logger.debug(
                         "Writing release version '$releaseVersion' to root build.gradle file")
                resolver.setProjectVersionOnFile(releaseVersion)
            }
            project.logger.lifecycle "**************************************************"
            project.logger.lifecycle("Setting project version to '$releaseVersion'")
            project.rootProject.ext.set("releaseVersion", releaseVersion)
        }

       project.version = project.rootProject.releaseVersion

        // Perform release tasks of all projects. Do this only once
        if (!project.rootProject.hasProperty("publishedProjects")) {
            project.logger.lifecycle "**************************************************"
            project.logger.lifecycle "Beginning the release task for ${tagPrefix}${project.version}"

            commitVersionFile("Release of version v$project.version", resolver)
            createReleaseTag(resolver.getTagName(tagPrefix, versionSuffix))

            // Prepare next release version's snapshot
            String nextVersion = getNextVersion()
            project.logger.lifecycle("\nUpdating project version to $nextVersion")

            resolver.setProjectVersionOnFile(nextVersion)
            commitVersionFile("Creating new $nextVersion version after release", resolver)

            // Push all those changes we had been making
            if (push) {
                pushChanges(resolver.getTagName(tagPrefix, versionSuffix))
            }
            project.logger.lifecycle "**************************************************"
            project.rootProject.ext.set("publishedProjects", true)
        }
    }

    /**
     * Commits the version file with changes made during release process.
     * @param msg the commit message
     * @param releaseExtension a reference to the release extension
     */
    def commitVersionFile(String msg, VersionResolver resolver) {
        project.getLogger().info("Committing version file: $msg")
        git 'commit', '-m', "\"$msg\"", ':/' + resolver.versionFile.name
    }

    /**
     * Creates a git tag of the project in preparation for release
     * @param tagName tag to be created
     */
    def createReleaseTag(String tagName) {
        project.getLogger().debug("Creating release tag: $tagName")
        git 'tag', '-a', tagName, "-m Release $tagName"
    }

    /**
     * Updates the projects version to a pre-release version for the next development cycle.
     * @param currentVersion release version information
     * @param suffix the pre-release suffix
     */
    String getNextVersion() {
       String version = project.version.toString()
       def versionInfo = VersionUpgradeStrategyFactory.parseVersionInfo(version - versionSuffix)
       int nextPatch = versionInfo.patch + 1
        return "${versionInfo.major}.${versionInfo.minor}.${nextPatch}${versionSuffix}".toString()
    }

    /**
     * Pushes commited changes to git
     * @param tag tag to be pushed to git
     */
    def pushChanges(String tag) {
        project.getLogger().debug('Pushing changes to repository')
        git 'push', 'origin', tag
        git 'push', 'origin', 'HEAD'
    }

    /**
     * This acts as a git command runner.
     * @param arguments arguments to the git command
     */
    def git(Object[] arguments) {
        project.getLogger().debug("git $arguments")
        def output = new ByteArrayOutputStream()

        // TODO: evaluate a better wait to catch git command execution return.
        // The .assertNormalExitValue will throw an exception when you do gradlew release and a commit is already made..
        // works fine for gradlew release[Major/Minor]Version...
        // We need a better way to tell the user what occurred without throwing an except which is what the assert does
        project.exec {
            executable 'git'
            args arguments
            standardOutput output
            ignoreExitValue = true
        }
        //.assertNormalExitValue()

        String gitOutput = output.toString().trim()
        if (!gitOutput.isEmpty()) {
            project.getLogger().debug(gitOutput)
        }
    }

    /**
     * Resolves Semantic versioning upgrade strategy based on the release task called by the user
     * @param taskNames all gradle tasks that are called by the user
     * @param versionSuffix project version suffix
     * @return {@link com.ngc.seaside.gradle.tasks.release.IVersionUpgradeStrategy}
     */
    static IVersionUpgradeStrategy resolveVersionUpgradeStrategy(List<String> taskNames, String versionSuffix) {
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
