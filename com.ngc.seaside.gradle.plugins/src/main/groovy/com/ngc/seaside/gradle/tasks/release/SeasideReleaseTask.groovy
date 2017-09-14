package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class SeasideReleaseTask extends DefaultTask {

    @TaskAction
    def release() {
        println "**************************************************"
        println "Beginning the release task for $project.version"
        SeasideReleaseExtension releaseExtension = project.getExtensions().getByType(SeasideReleaseExtension.class)

        commitVersionFile("Release of version $project.version", releaseExtension)
        createReleaseTag(releaseExtension.tagName)

        // TODO: add nexus functionality here
        // If nexus system property (-Pnexus) == true
        // run uploadArchives to upload release to nexus

        // Prepare next release version's snapshot
        String nextVersion = getNextVersion(project.version as String, releaseExtension.versionSuffix)
        println("Updating '$releaseExtension.versionFile' version to $nextVersion")

        releaseExtension.setVersionOnFile(nextVersion)
        commitVersionFile("Creating new $nextVersion version after release", releaseExtension)

        // Push all those changes we had been making
        if (releaseExtension.push) {
            pushChanges(releaseExtension.tagName)
        }
        println "**************************************************"
    }

    def commitVersionFile(String msg, SeasideReleaseExtension releaseExtension) {
        project.getLogger().info("Committing version file: $msg")
        git 'commit', '-m', "\"$msg\"", ':/' + releaseExtension.versionFile.name
    }

    def createReleaseTag(String tagName) {
        project.getLogger().debug("Creating release tag: $tagName")
        git 'tag', '-a', tagName, "-m Release $tagName"
    }

    def static getNextVersion(String currentVersion, String suffix) {
        def versionInfo = VersionUpgradeStrategyFactory.parseVersionInfo(currentVersion - suffix)
        int nextPatch = versionInfo.patch + 1
        "${versionInfo.major}.${versionInfo.minor}.${nextPatch}${suffix}" as String
    }

    def pushChanges(String tag) {
        project.getLogger().debug('Pushing changes to repository')
        git 'push', 'origin', tag
        git 'push', 'origin', 'HEAD'
    }

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
