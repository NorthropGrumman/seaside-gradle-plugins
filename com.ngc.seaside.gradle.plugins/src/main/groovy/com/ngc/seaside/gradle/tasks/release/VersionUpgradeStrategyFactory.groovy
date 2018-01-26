package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.GradleException

/**
 * Factory class for semantic version upgrade strategy
 */
final class VersionUpgradeStrategyFactory {


    /**
     * Creates {@link VersionInfo} object from String
     * @param version version to be create
     * @return {@link VersionInfo} instance
     */
    static VersionInfo parseVersionInfo(String version) {
        def (String majorPart, String minorPart, String patchPart) = version.tokenize('.')

        if (majorPart == null || minorPart == null || patchPart == null) {
            throw new GradleException("Invalid version '$version' (must follow 'major.minor.patch' semantics)")
        }

        int major = parsePart(majorPart, version, 'major')
        int minor = parsePart(minorPart, version, 'minor')
        int patch = parsePart(patchPart, version, 'patch')

        new VersionInfo(major, minor, patch)
    }

    /**
     * Converts parts of the semantic version string to integer
     * @param part defines which part of the version to parse (Major.Minor.Patch)
     * @param version version to be parsed
     * @param partName major or minor or patch
     * @return integer of version part converted
     */
    private static int parsePart(String part, String version, String partName) {
        try {
            return Integer.parseInt(part)
        } catch (NumberFormatException nfe) {
            String msg = "Invalid version '$version' (could not parse $partName part of expected 'major.minor.patch' format)"
            throw new GradleException(msg, nfe)
        }
    }

    /**
     * Strategy that creates a major version upgrade
     * @param versionSuffix suffix to from extracted from the pre-release version
     * @return {@link IVersionUpgradeStrategy} instance
     */
    static IVersionUpgradeStrategy createMajorVersionUpgradeStrategy(String versionSuffix) {
        return new IVersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                VersionInfo info = parseVersionInfo(currentVersion - versionSuffix)
                (info.major + 1) + ".0.0"
            }
        }
    }

    /**
     * Strategy that creates a minor version upgrade
     * @param versionSuffix suffix to from extracted from the pre-release version
     * @return {@link IVersionUpgradeStrategy} instance
     */
    static IVersionUpgradeStrategy createMinorVersionUpgradeStrategy(String versionSuffix) {
        return new IVersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                VersionInfo info = parseVersionInfo(currentVersion - versionSuffix)
                "$info.major." + (info.minor + 1) + ".0"
            }
        }
    }

    /**
     * Strategy that creates a patch version upgrade
     * @param versionSuffix suffix to from extracted from the pre-release version
     * @return {@link IVersionUpgradeStrategy} instance
     */
    static IVersionUpgradeStrategy createPatchVersionUpgradeStrategy(String versionSuffix) {
        return new IVersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                VersionInfo info = parseVersionInfo(currentVersion - versionSuffix)
                "$info.major." + "$info.minor." + (info.patch + 1)
            }
        }
    }

    /**
     * Strategy that creates a snapshot version upgrade
     * @param versionSuffix suffix to from extracted from the pre-release version
     * @return {@link IVersionUpgradeStrategy} instance
     */
    static IVersionUpgradeStrategy createSnapshotVersionUpgradeStrategy() {
        return new IVersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                currentVersion
            }
        }
    }

    /**
     * POJO to store semantic version information
     */
    static final class VersionInfo {

        final int major
        final int minor
        final int patch

        private VersionInfo(major, minor, patch) {
            this.major = major
            this.minor = minor
            this.patch = patch
        }

    }

}
