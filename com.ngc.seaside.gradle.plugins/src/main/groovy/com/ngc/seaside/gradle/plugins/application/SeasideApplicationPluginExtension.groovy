package com.ngc.seaside.gradle.plugins.application

class SeasideApplicationPluginExtension {
    String mainClassName
    String appHomeVarName
    String distributionName
    String installationDir
    String startScriptWindows
    String startScriptUnix
    List<String> includeDistributionDirs
    Map<String, String> appSystemProperties
}
