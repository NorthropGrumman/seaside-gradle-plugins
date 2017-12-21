package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ReleaseTask extends DefaultTask {

    private VersionResolver resolver = new VersionResolver(project)

    private SeasideReleaseExtension releaseExtension = project.extensions.getByType(SeasideReleaseExtension.class)

    boolean firstPass = false

    ReleaseType releaseType

    @TaskAction
    def release() {
        if(firstPass) {
            createNewReleaseVersionIfNecessary()
            project.version = project.rootProject.releaseVersion
        } else {
            releaseAllProjectsIfNecessary()
        }
    }

    private void createNewReleaseVersionIfNecessary() {
        if (!isReleaseVersionSet()) {
            def currentProjectVersion = resolver.getProjectVersion(releaseType)
            def newReleaseVersion = getTheReleaseVersion(currentProjectVersion)
            setTheNewReleaseVersion(newReleaseVersion)
            setTheReleaseVersionProjectProperty(newReleaseVersion)
        }
    }

    private boolean isReleaseVersionSet() {
        return project.rootProject.hasProperty("releaseVersion")
    }

    private String getTheReleaseVersion(String currentProjectVersion) {
        def upgradeStrategy = resolver.resolveVersionUpgradeStrategy(releaseType)
        String newReleaseVersion = upgradeStrategy.getVersion(currentProjectVersion)
        project.logger.info("Using release version '$newReleaseVersion'")
        return newReleaseVersion
    }

    private void setTheNewReleaseVersion(String newReleaseVersion) {
        if (!isDryRun()) {
            project.logger.lifecycle("Setting version in root build.gradle to $newReleaseVersion")
            resolver.setProjectVersionOnFile(newReleaseVersion)
        } else {
            project.logger.lifecycle("Dry Run >> Would have set version in root build.gradle to $newReleaseVersion")
        }
    }

    private boolean isDryRun() {
        return !(releaseExtension.push && releaseExtension.commitChanges && releaseExtension.uploadArtifacts)
    }

    private void setTheReleaseVersionProjectProperty(String newReleaseVersion) {
        project.rootProject.ext.set("releaseVersion", newReleaseVersion)

        String dryRunHeader = (isDryRun()) ? "Dry Run >>" : ""
        project.logger.lifecycle("$dryRunHeader Set project version to '$newReleaseVersion'")
    }

    private void releaseAllProjectsIfNecessary() {
        String dryRunHeader = (isDryRun()) ? "Dry Run >>" : ""
        if (!areAllProjectsReleased()) {
            project.logger.lifecycle("$dryRunHeader Beginning the release task for " +
                                     "${releaseExtension.tagPrefix}${project.version}")
            tagTheRelease()
            persistTheNewProjectVersion()
            pushTheChangesIfNecessary()
            setThePublishedProjectsProjectProperty()
        }
    }

    private boolean areAllProjectsReleased() {
        return project.rootProject.hasProperty("publishedProjects")
    }

    private void tagTheRelease() {
        commitVersionFileWithMessage("Release of version v$project.version")
        createReleaseTag(resolver.getTagName(releaseExtension.tagPrefix, releaseExtension.versionSuffix))
    }

    private void commitVersionFileWithMessage(String msg) {
        if (releaseExtension.commitChanges) {
            git "commit", "-m", "\"$msg\"", ":/$resolver.versionFile.name"
            project.logger.info("Committed version file: $msg")
        }

        if (isDryRun()) {
            project.logger.lifecycle("Dry Run >> Would have committed version file: $msg")
        }
    }

    private void git(Object[] arguments) {
        project.logger.debug("Will run: git $arguments")
        def output = new ByteArrayOutputStream()

        project.exec {
            executable "git"
            args arguments
            standardOutput output
            ignoreExitValue = true
        }

        output = output.toString().trim()
        if (!output.isEmpty()) {
            project.logger.debug(output)
        }
    }

    private void createReleaseTag(String tagName) {
        if (releaseExtension.commitChanges) {
            git "tag", "-a", tagName, "-m Release of $tagName"
            project.logger.debug("Created release tag: $tagName")
        }

        if (isDryRun()) {
            project.logger.lifecycle("Dry Run >> Would have created release tag: $tagName")
        }
    }

    private void persistTheNewProjectVersion() {
        String nextVersion = getNextVersion()
        String dryRunHeader = (isDryRun()) ? "Dry Run >>" : ""
        if (!isDryRun()) {
            resolver.setProjectVersionOnFile(nextVersion)
        }
        commitVersionFileWithMessage("Creating new $nextVersion version after release")
        project.logger.lifecycle("\n$dryRunHeader Updated project version to $nextVersion")
    }

    private String getNextVersion() {
        def (major, minor, patch) = calculateNextVersion()
        return "${major}.${minor}.${patch}${releaseExtension.versionSuffix}".toString()
    }

    private List<Integer> calculateNextVersion() {
        String versionWithoutSuffix = project.version.toString() - releaseExtension.versionSuffix
        def version = VersionUpgradeStrategyFactory.parseVersionInfo(versionWithoutSuffix)
        return [version.major, version.minor, version.patch + 1]
    }

    private void pushTheChangesIfNecessary() {
        if (releaseExtension.push) {
            pushChanges(resolver.getTagName(releaseExtension.tagPrefix, releaseExtension.versionSuffix))
        }

        if (isDryRun()) {
            project.logger.lifecycle("Dry Run >> Would have pushed changes to remote")
        }
    }

    private void pushChanges(String tag) {
        git "push", "origin", tag
        git "push", "origin", "HEAD"
    }

    private void setThePublishedProjectsProjectProperty() {
        project.rootProject.ext.set("publishedProjects", true)
    }
}
