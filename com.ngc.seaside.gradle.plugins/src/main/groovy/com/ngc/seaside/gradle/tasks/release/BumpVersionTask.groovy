package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.util.ProjectUtil
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class BumpVersionTask extends DefaultTask {

   boolean dryRun
   boolean commitChanges
   boolean push

   String tagPrefix
   String versionSuffix
   private VersionResolver resolver

   /**
    * CTOR
    */
   BumpVersionTask(){
      resolver = new VersionResolver(project)
      resolver.enforceVersionSuffix = true
   }

   def prepareForReleaseIfNeeded(ReleaseType releaseType) {
      project.gradle.startParameter.taskNames.contains(name)
   }

   /**
    * function required to be a task within the
    * gradle framework and is the entry point for
    * gradle
    *
    * @return
    */
   @TaskAction
   def bumpTheVersion() {
//      Preconditions.checkState(
//              ProjectUtil.isExtensionSet(project),
//              "Release task executing but prepareForReleaseIfNeeded() not invoked during configuration phase!")
       getReleaseExtensionSettings()
       //bumpVersion()
   }

//   private void bumpVersion() {
//      if (!isReleaseVersionSet()) {
//         def currentProjectVersion = resolver.getProjectVersion(releaseType)
//         def newReleaseVersion = getTheReleaseVersion(currentProjectVersion)
//         setTheNewReleaseVersion(newReleaseVersion)
//         setTheReleaseVersionProjectProperty(newReleaseVersion)
//      }
//   }
//
//   private boolean isReleaseVersionSet() {
//      return project.rootProject.hasProperty("releaseVersion")
//   }
//
//   private String getTheReleaseVersion(String currentProjectVersion) {
//      def upgradeStrategy = resolver.resolveVersionUpgradeStrategy(releaseType)
//      String newReleaseVersion = upgradeStrategy.getVersion(currentProjectVersion)
//      project.logger.info("Using release version '$newReleaseVersion'")
//      return newReleaseVersion
//   }
//
//   private void setTheNewReleaseVersion(String newReleaseVersion) {
//      if (!dryRun) {
//         project.logger.lifecycle("Setting version in root build.gradle to $newReleaseVersion")
//         resolver.setProjectVersionOnFile(newReleaseVersion)
//      } else {
//         project.logger.lifecycle("Dry Run >> Would have set version in root build.gradle to $newReleaseVersion")
//      }
//   }
//
//   private void setTheReleaseVersionProjectProperty(String newReleaseVersion) {
//      project.rootProject.ext.set("releaseVersion", newReleaseVersion)
//
//      String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
//      project.logger.lifecycle("$dryRunHeader Set project version to '$newReleaseVersion'")
//   }
//
//   private void releaseAllProjectsIfNecessary() {
//      String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
//      if (!areAllProjectsReleased()) {
//         project.logger.lifecycle("$dryRunHeader Beginning the release task for " +
//                 "${tagPrefix}${project.version}")
//         tagTheRelease()
//         persistTheNewProjectVersion()
//         pushTheChangesIfNecessary()
//         setThePublishedProjectsProjectProperty()
//      }
//   }
//
//   private boolean areAllProjectsReleased() {
//      return project.rootProject.hasProperty("publishedProjects")
//   }
//
//   private void tagTheRelease() {
//      commitVersionFileWithMessage("Release of version v$project.version")
//      createReleaseTag(resolver.getTagName(tagPrefix, versionSuffix))
//   }
//
//   private void commitVersionFileWithMessage(String msg) {
//      if (commitChanges && !dryRun) {
//         git "commit", "-m", "\"$msg\"", "$resolver.versionFile.absolutePath"
//         project.logger.info("Committed version file: $msg")
//      }
//
//      if (dryRun) {
//         project.logger.lifecycle("Dry Run >> Would have committed version file: $msg")
//      }
//   }
//
//   private void git(Object[] arguments) {
//      project.logger.debug("Will run: git $arguments")
//      def output = new ByteArrayOutputStream()
//
//      project.exec {
//         executable "git"
//         args arguments
//         standardOutput output
//         ignoreExitValue = true
//      }
//
//      output = output.toString().trim()
//      if (!output.isEmpty()) {
//         project.logger.debug(output)
//      }
//   }
//
//   private void createReleaseTag(String tagName) {
//      if (commitChanges && !dryRun) {
//         git "tag", "-a", tagName, "-m Release of $tagName"
//         project.logger.debug("Created release tag: $tagName")
//      }
//
//      if (dryRun) {
//         project.logger.lifecycle("Dry Run >> Would have created release tag: $tagName")
//      }
//   }
//
//   private void persistTheNewProjectVersion() {
//      String nextVersion = getNextVersion()
//      String dryRunHeader = (dryRun) ? "Dry Run >>" : ""
//      if (!dryRun) {
//         resolver.setProjectVersionOnFile(nextVersion)
//      }
//      commitVersionFileWithMessage("Creating new $nextVersion version after release")
//      project.logger.lifecycle("\n$dryRunHeader Updated project version to $nextVersion")
//   }
//
//   private String getNextVersion() {
//      def (major, minor, patch) = calculateNextVersion()
//      return "${major}.${minor}.${patch}${versionSuffix}".toString()
//   }
//
//   private List<Integer> calculateNextVersion() {
//      String versionWithoutSuffix = project.version.toString() - versionSuffix
//      def version = VersionUpgradeStrategyFactory.parseVersionInfo(versionWithoutSuffix)
//      return [version.major, version.minor, version.patch + 1]
//   }
//
//   private void pushTheChangesIfNecessary() {
//      if (push && !dryRun) {
//         pushChanges(resolver.getTagName(tagPrefix, versionSuffix))
//      }
//
//      if (dryRun) {
//         project.logger.lifecycle("Dry Run >> Would have pushed changes to remote")
//      }
//   }
//
//   private void pushChanges(String tag) {
//      git "push", "origin", tag
//      git "push", "origin", "HEAD"
//   }
//
//   private void setThePublishedProjectsProjectProperty() {
//      project.rootProject.ext.set("publishedProjects", true)
//   }

   private void getReleaseExtensionSettings() {
      commitChanges = ProjectUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).commitChanges
      push = ProjectUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).push
      versionSuffix = ProjectUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).versionSuffix
   }

}
