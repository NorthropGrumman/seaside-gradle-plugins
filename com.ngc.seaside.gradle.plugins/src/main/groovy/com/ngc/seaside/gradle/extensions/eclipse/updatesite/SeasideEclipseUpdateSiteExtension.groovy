package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import com.google.common.base.Preconditions
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class SeasideEclipseUpdateSiteExtension {
    public String archiveName
    public String cacheDirectory

    public String eclipseVersion
    public String eclipseArchiveName
    public String eclipseDownloadUrl
    public String eclipsePluginsDirectory

    public String linuxDownloadUrl
    public String linuxEclipseVersion
    public String windowsDownloadUrl
    public String windowsEclipseVersion

    SeasideEclipseUpdateSiteExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "caches", "eclipse")
    }

    String getEclipseVersion() {
        Preconditions.checkNotNull("linuxEclipseVersion must be defined!", linuxEclipseVersion)
        Preconditions.checkNotNull("windowsEclipseVersion must be defined!", windowsEclipseVersion)
        Preconditions.checkState(
              OperatingSystem.current().isLinux() || OperatingSystem.current().isWindows(),
              "supported operating systems are Linux and Windows!"
        )

        return OperatingSystem.current().isLinux() ? linuxEclipseVersion : windowsEclipseVersion
    }

    String getEclipseDownloadUrl() {
        Preconditions.checkNotNull("linuxDownloadUrl must be defined!", linuxDownloadUrl)
        Preconditions.checkNotNull("windowsDownloadUrl must be defined!", windowsDownloadUrl)
        Preconditions.checkState(
              OperatingSystem.current().isLinux() || OperatingSystem.current().isWindows(),
              "supported operating systems are Linux and Windows!"
        )

        return OperatingSystem.current().isLinux() ? linuxDownloadUrl : windowsDownloadUrl
    }

    String getEclipsePluginsDirectory() {
        return Paths.get(cacheDirectory, getEclipseVersion(), "plugins")
    }

    String getEclipseArchiveName() {
        return Paths.get(cacheDirectory, "${getEclipseVersion()}.zip")
    }

    @Override
    String toString() {
        return "SeasideEclipseUpdateSiteExtension[" +
               "\n\tarchiveName=" + archiveName +
               "\n\tcacheDirectory=" + cacheDirectory +
               "\n\tlinuxDownloadUrl=" + linuxDownloadUrl +
               "\n\tlinuxEclipseVersion=" + linuxEclipseVersion +
               "\n\twindowsDownloadUrl=" + windowsDownloadUrl +
               "\n\twindowsEclipseVersion=" + windowsEclipseVersion +
               "\n\teclipseVersion=" + getEclipseVersion() +
               "\n\teclipseArchiveName=" + getEclipseArchiveName() +
               "\n\teclipseDownloadUrl=" + getEclipseDownloadUrl() +
               "\n\teclipsePluginsDirectory=" + getEclipsePluginsDirectory() +
               "\n]"
    }
}
