package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class SeasideReleaseTask extends DefaultTask {

    @TaskAction
    def release() {
        SeasideReleaseExtension releaseExtension = project.getExtensions().getByType(SeasideReleaseExtension.class)
        commitVersionFile("Release v$project.version", releaseExtension)
        createReleaseTag(releaseExtension.tagName)
        String nextVersion = getNextVersion(project.version as String, releaseExtension.versionSuffix)
        logger.debug("Updating '$releaseExtension.versionFile' contents to $nextVersion")
        releaseExtension.versionFile.text = nextVersion
        commitVersionFile("Prepare next release v$nextVersion", releaseExtension)
        if (releaseExtension.push) {
            pushChanges(releaseExtension.tagName)
        }
    }

    def commitVersionFile(String msg, SeasideReleaseExtension releaseExtension) {
        logger.debug("Committing version file: $msg")
        git 'commit', '-m', msg, releaseExtension.versionFile.name
    }

    def createReleaseTag(String tagName) {
        logger.debug("Creating release tag: $tagName")
        git 'tag', '-a', tagName, "-m Release $tagName"
    }

    def static getNextVersion(String currentVersion, String suffix) {
        def versionInfo = VersionUpgradeStrategyFactory.parseVersionInfo(currentVersion - suffix)
        int nextPatch = versionInfo.patch + 1
        "$versionInfo.major.$versionInfo.minor.$nextPatch$suffix" as String
    }

    def pushChanges(String tag) {
        logger.debug('Pushing changes to repository')
        git 'push', 'origin', tag
        git 'push', 'origin', 'HEAD'
    }

    def git(Object[] arguments) {
        logger.debug("git $arguments")
        def output = new ByteArrayOutputStream()
        project.exec {
            executable 'git'
            args arguments
            standardOutput output
            ignoreExitValue = true
        }.assertNormalExitValue()
        String gitOutput = output.toString().trim()
        if (!gitOutput.isEmpty()) {
            logger.debug(gitOutput)
        }
    }

}
