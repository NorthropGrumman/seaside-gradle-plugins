/**
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
package com.ngc.seaside.gradle.plugins.version;

import com.ngc.seaside.gradle.plugins.release.ReleaseType;
import com.ngc.seaside.gradle.util.IResolver;
import com.ngc.seaside.gradle.util.Versions;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionResolver implements IResolver {

   private static final Pattern PATTERN = Pattern.compile(
         "^(?<beginning> \\s*? version \\s*? = \\s*? ([\"']?) )" +
         " (?<version> \\d+?(?:\\.\\d+?)+? )" +
         " (?<suffix> [-\\.][A-Za-z]+ )?" +
         " (?<ending> \\2\\s*? )$",
         Pattern.MULTILINE | Pattern.COMMENTS);
   private static final String DEFAULT_VERSION_FILENAME = "../versions.gradle";

   private Logger logger;
   private File versionFile;
   private Project project;
   private boolean enforceVersionSuffix = true;

   public VersionResolver(Project p) {
      project = p;
      VersionResolver resolver = project.getExtensions().findByType(VersionResolver.class);
      if (resolver == null) {
         String versionFilename = project.hasProperty("versionsFile")
                                  ? project.property("versionsFile").toString()
                                  : DEFAULT_VERSION_FILENAME;
         File versionFileForRootProject = project.getRootProject().file(versionFilename);
         versionFile = versionFileForRootProject != null && versionFileForRootProject.exists()
                       ? versionFileForRootProject
                       : project.getRootProject().getBuildFile();
      } else {
         versionFile = resolver.versionFile;
      }
      logger = project.getLogger();
   }

   public boolean isEnforceVersionSuffix() {
      return enforceVersionSuffix;
   }

   public VersionResolver setEnforceVersionSuffix(boolean enforceVersionSuffix) {
      this.enforceVersionSuffix = enforceVersionSuffix;
      return this;
   }

   public String getProjectVersion() {
      String text;
      try {
         text = new String(Files.readAllBytes(versionFile.toPath())).trim();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
      return getSemanticVersion(text);
   }

   public String getUpdatedProjectVersionForRelease(ReleaseType releaseType) {
      return resolveVersionUpgradeStrategy(releaseType).getVersion(getProjectVersion());
   }

   protected String getSemanticVersion(String input) {
      Matcher matcher = PATTERN.matcher(input.trim());
      StringBuilder sb = new StringBuilder();

      if (matcher.find()) {
         String version = matcher.group("version");
         String suffix = matcher.group("suffix");
         if (version != null) {
            sb.append(version);
            if (suffix == null && enforceVersionSuffix) {
               String message = String.format("Missing project version (%s%s}) suffix: %s",
                                              version,
                                              suffix,
                                              Versions.VERSION_SUFFIX);
               throw new GradleException(message);
            } else if (suffix != null) {
               sb.append(suffix);
            }
            return sb.toString();
         } else {
            String message = String.format("Missing project version (%s%s})", version, suffix);
            throw new GradleException(message);
         }
      } else {
         logger.error("\nFailed to extract semantic versioning information from file contents: '" + input + "'");
         logger.error("Does the version information follow Semantic Versioning Format?");
         throw new GradleException("File contents: '" + input + "'\ndo not follow semantic versioning format");
      }
   }

   public void setProjectVersionOnFile(String newVersion) throws IOException {
      String text = new String(Files.readAllBytes(versionFile.toPath()));
      Matcher m = PATTERN.matcher(text);
      text = m.replaceFirst("${beginning}" + newVersion + "${ending}");
      Files.write(versionFile.toPath(), text.getBytes());
   }

   public String getTagName(String tagPrefix, String versionSuffix) {
      String suffixLessProjectVersion = project.getVersion().toString();
      if (suffixLessProjectVersion.endsWith(versionSuffix)) {
         suffixLessProjectVersion =
               suffixLessProjectVersion.substring(0, suffixLessProjectVersion.length() - versionSuffix.length());
      }
      return tagPrefix + suffixLessProjectVersion;
   }

   public File getVersionFile() {
      return versionFile;
   }

   public void setVersionFile(File file) {
      versionFile = file;
   }

   public static IVersionUpgradeStrategy resolveVersionUpgradeStrategy(ReleaseType releaseType) {
      switch (releaseType) {
         case MAJOR:
            return VersionUpgradeStrategyFactory.createMajorVersionUpgradeStrategy(Versions.VERSION_SUFFIX);
         case MINOR:
            return VersionUpgradeStrategyFactory.createMinorVersionUpgradeStrategy(Versions.VERSION_SUFFIX);
         case PATCH:
            return VersionUpgradeStrategyFactory.createPatchVersionUpgradeStrategy(Versions.VERSION_SUFFIX);
         default:
            return VersionUpgradeStrategyFactory.createSnapshotVersionUpgradeStrategy();
      }
   }

}
