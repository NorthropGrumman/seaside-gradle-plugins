package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.util.eclipse.EclipsePropertyUtil
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class SeasideEclipseUpdateSiteExtension {
    public String updateSiteArchiveName
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
        updateSiteArchiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "caches", "eclipse")
    }

    String getEclipseDownloadUrl() {
        if (eclipseDownloadUrl == null) {
            Preconditions.checkNotNull(linuxDownloadUrl, "linuxDownloadUrl must be defined!")
            Preconditions.checkNotNull(windowsDownloadUrl, "windowsDownloadUrl must be defined!")
            Preconditions.checkState(
                  OperatingSystem.current().isLinux() || OperatingSystem.current().isWindows(),
                  "supported operating systems are Linux and Windows!"
            )

            eclipseDownloadUrl = OperatingSystem.current().isLinux() ? linuxDownloadUrl : windowsDownloadUrl
        }

        return eclipseDownloadUrl
    }

    String getEclipsePluginsDirectory() {
        if (eclipsePluginsDirectory == null) {
            eclipsePluginsDirectory = Paths.get(cacheDirectory, EclipsePropertyUtil.getEclipseVersion(), "plugins")
        }

        return eclipsePluginsDirectory
    }

    String getEclipseArchiveName() {
        if (eclipseArchiveName == null) {
            eclipseArchiveName = Paths.get(cacheDirectory, "${EclipsePropertyUtil.getEclipseVersion()}.zip")
        }

        return eclipseArchiveName
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
               "\n\teclipseVersion=" + eclipseVersion +
               "\n\teclipseArchiveName=" + eclipseArchiveName +
               "\n\teclipseDownloadUrl=" + eclipseDownloadUrl +
               "\n\teclipsePluginsDirectory=" + eclipsePluginsDirectory +
               "\n]"
    }
}
