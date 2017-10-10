package com.ngc.seaside.gradle.plugins.util

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionResolver {
   private static final Pattern PATTERN =
      Pattern.compile(
         "^\\s*version\\s*=\\s*[\"']?(?!\\.)(\\d+(\\.\\d+)+)([-.][A-Z]+)?[\"']?(?![\\d.])\$",
         Pattern.MULTILINE
      )

   private static String versionSuffix = "-SNAPSHOT"
   private Logger logger
   private File versionFile
   private Project project

   VersionResolver(Project p) {
      project = p
      versionFile = new File([project.rootProject.projectDir, 'build.gradle'].join(File.separator))
      logger = project.logger
   }

   String getProjectVersion(boolean enforceVersionSuffix) throws Exception {
      return getSemanticVersion(versionFile.text.trim(), enforceVersionSuffix)
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
               logger.error("Missing project version (${version}${suffix})  suffix: $versionSuffix")
               throw new GradleException("Missing project version (${version}${suffix}) suffix:$versionSuffix")
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
}
