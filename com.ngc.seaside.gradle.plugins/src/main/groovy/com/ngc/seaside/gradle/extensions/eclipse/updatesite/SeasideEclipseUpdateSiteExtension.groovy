package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import org.gradle.api.Project

import java.nio.file.Paths

class SeasideEclipseUpdateSiteExtension {
    public String updateSiteArchiveName
    public String cacheDirectory

    public String linuxDownloadUrl
    public String linuxEclipseVersion
    public String windowsDownloadUrl
    public String windowsEclipseVersion

    SeasideEclipseUpdateSiteExtension(Project project) {
        updateSiteArchiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "caches", "eclipse")
    }

    @Override
    String toString() {
        return "SeasideEclipseUpdateSiteExtension[" +
               "\n\tupdateSiteArchiveName=" + updateSiteArchiveName +
               "\n\tcacheDirectory=" + cacheDirectory +
               "\n\tlinuxDownloadUrl=" + linuxDownloadUrl +
               "\n\tlinuxEclipseVersion=" + linuxEclipseVersion +
               "\n\twindowsDownloadUrl=" + windowsDownloadUrl +
               "\n\twindowsEclipseVersion=" + windowsEclipseVersion +
               "\n]"
    }
}
