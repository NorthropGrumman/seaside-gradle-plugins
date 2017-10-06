package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class SeasideReleaseTask extends DefaultTask {

    @TaskAction
    def release() {
        // Get extension reference
        SeasideReleaseExtension releaseExtension = project.getExtensions().getByType(SeasideReleaseExtension.class)

        // Perform release tasks of all projects. Do this only once
        if (!project.rootProject.hasProperty("publishedProjects")) {
            println "**************************************************"
            println "Beginning the release task for ${releaseExtension.tagPrefix}${project.version}"

            commitVersionFile("Release of version v$project.version", releaseExtension)
            createReleaseTag(releaseExtension.tagName)

            // Prepare next release version's snapshot
            String nextVersion = getNextVersion(project.version as String, releaseExtension.versionSuffix)
            println("\nUpdating '$releaseExtension.versionFile' version to $nextVersion")

            releaseExtension.setVersionOnFile(nextVersion)
            commitVersionFile("Creating new $nextVersion version after release", releaseExtension)

            // Push all those changes we had been making
            if (releaseExtension.push) {
                pushChanges(releaseExtension.tagName)
            }
            println "**************************************************"
            project.rootProject.ext.set("publishedProjects", true)
        }
    }

    /**
     * Commits the version file with changes made during release process.
     * @param msg the commit message
     * @param releaseExtension a reference to the release extension
     */
    def commitVersionFile(String msg, SeasideReleaseExtension releaseExtension) {
        project.getLogger().info("Committing version file: $msg")
        git 'commit', '-m', "\"$msg\"", ':/' + releaseExtension.versionFile.name
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
    def static getNextVersion(String currentVersion, String suffix) {
        def versionInfo = VersionUpgradeStrategyFactory.parseVersionInfo(currentVersion - suffix)
        int nextPatch = versionInfo.patch + 1
        "${versionInfo.major}.${versionInfo.minor}.${nextPatch}${suffix}" as String
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
}
