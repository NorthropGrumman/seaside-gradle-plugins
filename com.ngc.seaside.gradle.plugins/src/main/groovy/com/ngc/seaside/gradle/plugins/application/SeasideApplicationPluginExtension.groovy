package com.ngc.seaside.gradle.plugins.application

class SeasideApplicationPluginExtension {
    String appHomeVarName
    String distributionName
    String distributionDir
    String startScriptWindows
    String startScriptUnix
    List<String> includeDistributionDirs
    Map<String, String> appSystemProperties
}
