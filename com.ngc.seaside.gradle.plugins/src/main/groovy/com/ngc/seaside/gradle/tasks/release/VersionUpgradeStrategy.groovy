package com.ngc.seaside.gradle.tasks.release

interface VersionUpgradeStrategy {

    String getVersion(String currentVersion)

}