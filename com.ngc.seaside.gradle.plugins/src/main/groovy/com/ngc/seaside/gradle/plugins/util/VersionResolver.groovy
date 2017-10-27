package com.ngc.seaside.gradle.plugins.util

import com.ngc.seaside.gradle.api.IResolver
import com.ngc.seaside.gradle.tasks.release.IVersionUpgradeStrategy
import com.ngc.seaside.gradle.tasks.release.VersionUpgradeStrategyFactory
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionResolver implements IResolver {
   public static final String RELEASE_TASK_NAME = "release"
   public static final String VERSION_SUFFIX = "-SNAPSHOT"
   public static final String RELEASE_MAJOR_VERSION_TASK_NAME = "releaseMajorVersion"
   public static final String RELEASE_MINOR_VERSION_TASK_NAME = "releaseMinorVersion"

   private static final Pattern PATTERN =
      Pattern.compile(
         "^\\s*version\\s*=\\s*[\"']?(?!\\.)(\\d+(\\.\\d+)+)([-.][A-Z]+)?[\"']?(?![\\d.])\$",
         Pattern.MULTILINE
      )

   private Logger logger
   private File versionFile
   private Project project

   VersionResolver(Project p) {
      project = p
      versionFile = new File([project.rootProject.projectDir, 'build.gradle'].join(File.separator))
      logger = project.logger
   }

   String getProjectVersion() throws Exception {
      def taskNames = project.gradle.startParameter.taskNames
      def isReleaseJob =
            taskNames.contains(RELEASE_TASK_NAME) ||
            taskNames.contains(RELEASE_MAJOR_VERSION_TASK_NAME) ||
            taskNames.contains(RELEASE_MINOR_VERSION_TASK_NAME)
      def versionFromFile =  getSemanticVersion(versionFile.text.trim(), isReleaseJob)

      return resolveVersionUpgradeStrategy(taskNames).getVersion(versionFromFile)
   }

   protected String getSemanticVersion(String input, boolean enforceVersionSuffix = false) {
      Matcher matcher = PATTERN.matcher(input.trim())
      StringBuilder sb = new StringBuilder()

      if (matcher.find()) {
         String version = matcher.group(1)
         String suffix = matcher.group(3)
         if (version) {
            sb.append(version)
            if (!suffix && enforceVersionSuffix) {
               logger.error("Missing project version (${version}${suffix})  suffix: $VERSION_SUFFIX")
               throw new GradleException("Missing project version (${version}${suffix}) suffix:$VERSION_SUFFIX")
            } else if (suffix) {
               sb.append(suffix)
            }
            return sb.toString()
         } else {
            logger.error("Missing project version (${version}${suffix})")
            throw new GradleException("Missing project version (${version}${suffix})")
         }
      } else {
         logger.error("\nFailed to extract semantic versioning information from file contents:$input")
         logger.error("Does the version information follow Semantic Versioning Format?\n")
         throw new GradleException("Version:$input \ndoes not follow semantic versioning format")
      }
   }

   void setProjectVersionOnFile(String newVersion) {
      versionFile.text = versionFile.text.replaceFirst(PATTERN, "\tversion = \'$newVersion\'")
   }

   String getTagName(String tagPrefix, String versionSuffix) {
      return tagPrefix + "$project.version" - versionSuffix
   }

   File getVersionFile() {
      return versionFile
   }


   static IVersionUpgradeStrategy resolveVersionUpgradeStrategy(List<String> taskNames) {
      if (isMajorVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(VERSION_SUFFIX)
      } else if (isMinorVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(VERSION_SUFFIX)
      } else if (isPatchVersionRelease(taskNames)) {
         return VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(VERSION_SUFFIX)
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
