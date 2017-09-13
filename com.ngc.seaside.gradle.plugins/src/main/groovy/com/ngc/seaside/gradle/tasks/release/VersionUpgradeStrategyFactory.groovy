package com.ngc.seaside.gradle.tasks.release

import org.gradle.api.GradleException

final class VersionUpgradeStrategyFactory {

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

    private static int parsePart(String part, String version, String partName) {
        try {
            return Integer.parseInt(part)
        } catch (NumberFormatException nfe) {
            String msg = "Invalid version '$version' (could not parse $partName part of expected 'major.minor.patch' format)"
            throw new GradleException(msg, nfe)
        }
    }

    static VersionUpgradeStrategy createMajorVersionUpgradeStrategy(String versionSuffix) {
        return new VersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                VersionInfo info = parseVersionInfo(currentVersion - versionSuffix)
                (info.major + 1) + ".0.0"
            }
        }
    }

    static VersionUpgradeStrategy createMinorVersionUpgradeStrategy(String versionSuffix) {
        return new VersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                VersionInfo info = parseVersionInfo(currentVersion - versionSuffix)
                "$info.major." + (info.minor + 1) + ".0"
            }
        }
    }

    static VersionUpgradeStrategy createPatchVersionUpgradeStrategy(String versionSuffix) {
        return new VersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                currentVersion - versionSuffix
            }
        }
    }

    static VersionUpgradeStrategy createSnapshotVersionUpgradeStrategy() {
        return new VersionUpgradeStrategy() {
            @Override
            String getVersion(String currentVersion) {
                currentVersion
            }
        }
    }

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