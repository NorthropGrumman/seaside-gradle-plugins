package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import com.ngc.seaside.gradle.plugins.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ReleaseTask extends DefaultTask {
   public static final String RELEASE_TASK_NAME = "release"
   public static final String RELEASE_MAJOR_VERSION_TASK_NAME = "releaseMajorVersion"
   public static final String RELEASE_MINOR_VERSION_TASK_NAME = "releaseMinorVersion"

   private VersionResolver resolver = new VersionResolver(project)

   @Input String tagPrefix
   @Input String versionSuffix
   @Input boolean push
   @Input boolean commitChanges

   @TaskAction
   def release() {
      createNewReleaseVersionIfNecessary()
      project.version = project.rootProject.releaseVersion
      releaseAllProjectsIfNecessary()
   }

   private void createNewReleaseVersionIfNecessary() {
      if (!isReleaseVersionSet()) {
         def currentProjectVersion = resolver.getProjectVersion(true)
         def newReleaseVersion = getTheReleaseVersion(currentProjectVersion)
         setTheNewReleaseVersion(currentProjectVersion, newReleaseVersion)
         setTheReleaseVersionProjectProperty(newReleaseVersion)
      }
   }

   private boolean isReleaseVersionSet() {
      return project.rootProject.hasProperty("releaseVersion")
   }

   private String getTheReleaseVersion(String currentProjectVersion) {
      def taskNames = project.gradle.startParameter.taskNames
      def upgradeStrategy = resolveVersionUpgradeStrategy(taskNames, versionSuffix)
      String newReleaseVersion = upgradeStrategy.getVersion(currentProjectVersion)
      project.logger.debug("Using release version '$newReleaseVersion'")
      return newReleaseVersion
   }

   private void setTheNewReleaseVersion(String currentProjectVersion, String newReleaseVersion) {
      if (!isDryRun() && currentProjectVersion != newReleaseVersion)
         resolver.setProjectVersionOnFile(newReleaseVersion)
   }

   private boolean isDryRun() {
      return project.gradle.startParameter.dryRun
   }

   private void setTheReleaseVersionProjectProperty(String newReleaseVersion) {
      project.rootProject.ext.set("releaseVersion", newReleaseVersion)
      project.logger.lifecycle("Set project version to '$newReleaseVersion'")
   }

   private void releaseAllProjectsIfNecessary() {
      if (!areAllProjectsReleased()) {
         project.logger.lifecycle("Beginning the release task for ${tagPrefix}${project.version}")
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
      if (commitChanges){
         git "commit", "-m", "\"$msg\"", ":/$resolver.versionFile.name"
         project.logger.info("Committed version file: $msg")
      }
   }

   private void git(Object[] arguments) {
      project.logger.debug("Will run: git $arguments")
      def output = new ByteArrayOutputStream()

      // TODO: evaluate a better way to catch git command execution return.
      // The .assertNormalExitValue will throw an exception when you do gradlew release and a commit is already made..
      // works fine for gradlew release[Major/Minor]Version...
      // We need a better way to tell the user what occurred without throwing an exception
      project.exec {
         executable "git"
         args arguments
         standardOutput output
         ignoreExitValue = true
      }

      output = output.toString().trim()
      if (!output.isEmpty())
         project.logger.debug(output)
   }

   private void createReleaseTag(String tagName) {
      if(commitChanges) {
         git "tag", "-a", tagName, "-m Release $tagName"
         project.logger.debug("Created release tag: $tagName")
      }
   }

   private void persistTheNewProjectVersion() {
      String nextVersion = getNextVersion()
      resolver.setProjectVersionOnFile(nextVersion)
      commitVersionFileWithMessage("Creating new $nextVersion version after release")
      project.logger.lifecycle("\nUpdated project version to $nextVersion")
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
      if (push)
         pushChanges(resolver.getTagName(tagPrefix, versionSuffix))
   }

   private void pushChanges(String tag) {
      git "push", "origin", tag
      git "push", "origin", "HEAD"
   }

   private void setThePublishedProjectsProjectProperty() {
      project.rootProject.ext.set("publishedProjects", true)
   }

   static IVersionUpgradeStrategy resolveVersionUpgradeStrategy(List<String> taskNames, String versionSuffix) {
      if (isMajorVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(versionSuffix)
      } else if (isMinorVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(versionSuffix)
      } else if (isPatchVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(versionSuffix)
      } else {
         return VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy()
      }
   }

   private static boolean isPatchVersionRelease(List<String> taskNames) {
      return taskNames.contains(RELEASE_TASK_NAME)
   }

   private static boolean isMinorVersionRelease(List<String> taskNames) {
      return taskNames.contains(RELEASE_MINOR_VERSION_TASK_NAME)
   }

   private static boolean isMajorVersionRelease(List<String> taskNames) {
      return taskNames.contains(RELEASE_MAJOR_VERSION_TASK_NAME)
   }
}
