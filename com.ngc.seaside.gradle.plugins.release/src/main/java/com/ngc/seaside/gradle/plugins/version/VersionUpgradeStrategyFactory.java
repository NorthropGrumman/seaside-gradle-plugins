package com.ngc.seaside.gradle.plugins.version;

import org.gradle.api.GradleException;

/**
 * Factory class for semantic version upgrade strategy
 */
public final class VersionUpgradeStrategyFactory {

   /**
    * Creates {@link VersionInfo} object from the given version without the given suffix.
    * 
    * @param version version to be create
    * @param versionSuffixToRemove suffix to remove from the version
    * @return {@link VersionInfo} instance
    */
   public static VersionInfo parseVersionInfo(String version, String versionSuffixToRemove) {
      String suffixLessVersion = version;
      if (suffixLessVersion.endsWith(versionSuffixToRemove)) {
         suffixLessVersion = suffixLessVersion.substring(0,
            suffixLessVersion.length() - versionSuffixToRemove.length());
      }
      return parseVersionInfo(suffixLessVersion);
   }

   /**
    * Creates {@link VersionInfo} object from String.
    * 
    * @param version version to be create
    * @return {@link VersionInfo} instance
    */
   public static VersionInfo parseVersionInfo(String version) {
      String[] versionSplit = version.split("\\.");
      String majorPart = versionSplit.length > 0 ? versionSplit[0] : null;
      String minorPart = versionSplit.length > 1 ? versionSplit[1] : null;
      String patchPart = versionSplit.length > 2 ? versionSplit[2] : null;

      if (majorPart == null || minorPart == null || patchPart == null) {
         throw new GradleException("Invalid version '" + version + "' (must follow 'major.minor.patch' semantics)");
      }

      int major = parsePart(majorPart, version, "major");
      int minor = parsePart(minorPart, version, "minor");
      int patch = parsePart(patchPart, version, "patch");

      return new VersionInfo(major, minor, patch);
   }

   /**
    * Converts parts of the semantic version string to integer.
    * 
    * @param part defines which part of the version to parse (Major.Minor.Patch)
    * @param version version to be parsed
    * @param partName major or minor or patch
    * @return integer of version part converted
    */
   private static int parsePart(String part, String version, String partName) {
      try {
         return Integer.parseInt(part);
      } catch (NumberFormatException nfe) {
         String msg = "Invalid version '" + version + "' (could not parse " + partName
            + " part of expected 'major.minor.patch' format)";
         throw new GradleException(msg, nfe);
      }
   }

   /**
    * Strategy that creates a major version upgrade.
    * 
    * @param versionSuffix suffix to from extracted from the pre-release version
    * @return {@link IVersionUpgradeStrategy} instance
    */
   public static IVersionUpgradeStrategy createMajorVersionUpgradeStrategy(String versionSuffix) {
      return currentVersion -> {
         VersionInfo info = parseVersionInfo(currentVersion, versionSuffix);
         return (info.getMajor() + 1) + ".0.0";
      };
   }

   /**
    * Strategy that creates a minor version upgrade.
    * 
    * @param versionSuffix suffix to from extracted from the pre-release version
    * @return {@link IVersionUpgradeStrategy} instance
    */
   public static IVersionUpgradeStrategy createMinorVersionUpgradeStrategy(String versionSuffix) {
      return currentVersion -> {
         VersionInfo info = parseVersionInfo(currentVersion, versionSuffix);
         return info.getMajor() + "." + (info.getMinor() + 1) + ".0";
      };
   }

   /**
    * Strategy that creates a patch version upgrade.
    * 
    * @param versionSuffix suffix to from extracted from the pre-release version
    * @return {@link IVersionUpgradeStrategy} instance
    */
   public static IVersionUpgradeStrategy createPatchVersionUpgradeStrategy(String versionSuffix) {
      return currentVersion -> {
         VersionInfo info = parseVersionInfo(currentVersion, versionSuffix);
         return info.getMajor() + "." + info.getMinor() + "." + (info.getPatch() + 1);
      };
   }

   /**
    * Strategy that creates a snapshot version upgrade.
    * 
    * @return {@link IVersionUpgradeStrategy} instance
    */
   public static IVersionUpgradeStrategy createSnapshotVersionUpgradeStrategy() {
      return currentVersion -> currentVersion;
   }

   /**
    * POJO to store semantic version information.
    */
   public static final class VersionInfo {

      private final int major;
      private final int minor;
      private final int patch;

      private VersionInfo(int major, int minor, int patch) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      public int getPatch() {
         return this.patch;
      }

   }

}
