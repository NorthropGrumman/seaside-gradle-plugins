/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.release.task

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.release.ReleaseType
import com.ngc.seaside.gradle.plugins.release.ReleaseUtil
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.plugins.version.VersionUpgradeStrategyFactory

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ReleaseTask extends DefaultTask {
    private VersionResolver resolver

    /**
     * Defines the type of release this instance of the task is configured to perform.
     */
    private ReleaseType releaseType

    boolean dryRun

    boolean commitChanges

    boolean push

    String tagPrefix

    String versionSuffix

    ReleaseTask() {
        resolver = new VersionResolver(project)
        resolver.enforceVersionSuffix = true
    }

    /**
     * Sets the type of release to perform and prepares for a release by updating the version number if the this task
     * was invoked when Gradle was started.  This method should be invoked during the configuration phase before
     * {@link #release()} is invoked during the execution phase.
     */
    def prepareForReleaseIfNeeded(ReleaseType releaseType) {
        // TODO TH: This makes it harder to reuse this task.
        def isTaskInvoked = project.gradle.startParameter.taskNames.contains(name)
        // Disable this task if the task was not actually executed.
        enabled = isTaskInvoked

        // If the task was invoked, update the version number before the build actually executes.  This ensures that
        // tasks which do not use lazy property evaluation are configured correctly before they are executed.  If we
        // waited to do this during the execution phase, tasks would be configured to execute with the wrong version.
        if (isTaskInvoked) {
            project.logger.info("Preparing for a $releaseType release.")
            this.releaseType = releaseType
            createNewReleaseVersionIfNecessary()
            project.version = project.rootProject.releaseVersion
        }
    }

    @TaskAction
    def release() {
        // Require the plugin to be configured before executing.
        Preconditions.checkState(
              ReleaseUtil.isExtensionSet(project),
              "Release task executing but prepareForReleaseIfNeeded() not invoked during configuration phase!")
        getReleaseExtensionSettings()
        releaseAllProjectsIfNecessary()
    }

    private void createNewReleaseVersionIfNecessary() {
        if (!ReleaseUtil.isExtensionSet(project)) {
            def currentProjectVersion = resolver.getProjectVersion()
            def newReleaseVersion = getTheReleaseVersion(currentProjectVersion)
            setTheNewReleaseVersion(newReleaseVersion)
            setTheReleaseVersionProjectProperty(newReleaseVersion)
        }
    }

    private String getTheReleaseVersion(String currentProjectVersion) {
        def upgradeStrategy = resolver.resolveVersionUpgradeStrategy(releaseType)
        String newReleaseVersion = upgradeStrategy.getVersion(currentProjectVersion)
        project.logger.info("Using release version '$newReleaseVersion'")
        return newReleaseVersion
    }

    private void setTheNewReleaseVersion(String newReleaseVersion) {
        if (!dryRun) {
            project.logger.lifecycle("Setting version in root build.gradle to $newReleaseVersion")
            resolver.setProjectVersionOnFile(newReleaseVersion)
        } else {
            project.logger.lifecycle("Dry Run >> Would have set version in root build.gradle to $newReleaseVersion")
        }
    }

    private void setTheReleaseVersionProjectProperty(String newReleaseVersion) {
        project.rootProject.ext.set("releaseVersion", newReleaseVersion)

        String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
        project.logger.lifecycle("$dryRunHeader Set project version to '$newReleaseVersion'")
    }

    private void releaseAllProjectsIfNecessary() {
        String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
        if (!areAllProjectsReleased()) {
            project.logger.lifecycle("$dryRunHeader Beginning the release task for " +
                                     "${tagPrefix}${project.version}")
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
        createReleaseTag(resolver.getTagName(tagPrefix, versionSuffix))
    }

    private void commitVersionFileWithMessage(String msg) {
        if (commitChanges && !dryRun) {
            project.exec ReleaseUtil.git("commit", "-m", "\"$msg\"", "$resolver.versionFile.absolutePath")
            project.logger.info("Committed version file: $msg")
        }

        if (dryRun) {
            project.logger.lifecycle("Dry Run >> Would have committed version file: $msg")
        }
    }

    private void createReleaseTag(String tagName) {
        if (commitChanges && !dryRun) {
            project.exec ReleaseUtil.git("tag", "-a", tagName, "-m Release of $tagName")
            project.logger.debug("Created release tag: $tagName")
        }

        if (dryRun) {
            project.logger.lifecycle("Dry Run >> Would have created release tag: $tagName")
        }
    }

    private void persistTheNewProjectVersion() {
        String nextVersion = getNextVersion()
        String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
        if (!dryRun) {
            resolver.setProjectVersionOnFile(nextVersion)
        }
        commitVersionFileWithMessage("Creating new $nextVersion version after release")
        project.logger.lifecycle("\n$dryRunHeader Updated project version to $nextVersion")
    }

    private String getNextVersion() {
        def (major, minor, patch) = calculateNextVersion()
        return "${major}.${minor}.${patch}${versionSuffix}".toString()
    }

    private List<Integer> calculateNextVersion() {
        String versionWithoutSuffix = project.version.toString() - versionSuffix
        def version = VersionUpgradeStrategyFactory.parseVersionInfo(versionWithoutSuffix)
        return [version.major, version.minor, version.patch + 1]
    }

    private void pushTheChangesIfNecessary() {
        if (push && !dryRun) {
            pushChanges(resolver.getTagName(tagPrefix, versionSuffix))
        }

        if (dryRun) {
            project.logger.lifecycle("Dry Run >> Would have pushed changes to remote")
        }
    }

    private void pushChanges(String tag) {
        project.exec ReleaseUtil.git("push", "origin", tag)
        project.exec ReleaseUtil.git("push", "origin", "HEAD")
    }

    private void setThePublishedProjectsProjectProperty() {
        project.rootProject.ext.set("publishedProjects", true)
    }

    private void getReleaseExtensionSettings() {
        commitChanges = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).getCommitChanges()
        push = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).getPush()
        versionSuffix = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).getVersionSuffix()
        tagPrefix = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).getTagPrefix()
    }
}
