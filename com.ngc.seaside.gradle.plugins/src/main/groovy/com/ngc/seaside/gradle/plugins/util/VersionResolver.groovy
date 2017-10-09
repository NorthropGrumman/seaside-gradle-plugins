package com.ngc.seaside.gradle.plugins.util

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

class VersionResolver {
   private static String versionSuffix = "-SNAPSHOT"
   private Logger logger
   private static final Pattern PATTERN =
            Pattern.compile(
               "^\\s*version\\s*=\\s*[\"']?(?!\\.)(\\d+(\\.\\d+)+)([-.][A-Z]+)?[\"']?(?![\\d.])\$",
               Pattern.MULTILINE
            )
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

   /**
    * Modifies the version number on the version file
    * @param newVersion the new version to write to version file
    */
   void setProjectVersionOnFile(String newVersion) {
      versionFile.text = versionFile.text.replaceFirst(PATTERN, "\tversion = \'$newVersion\'")
   }

   /**
    * Returns the tagName that will be used for committing the released project
    * @return {@link String} of git tag name
    */
   String getTagName(String tagPrefix, String versionSuffix) {
      return tagPrefix + "$project.version" - versionSuffix
   }

   File getVersionFile() {
      return versionFile
   }
}
