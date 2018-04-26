package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import com.google.common.base.Preconditions
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

/**
 * Extension for the seaside eclipse update site plugin.
 */
class SeasideEclipseUpdateSiteExtension {
    
    /**
     * The archive name of the update site zip. By default this is {@code group.artifact-version.zip}.
     */
    String updateSiteArchiveName

    /**
     * The directory used to cache the downloaded eclipse distribution. This property is optional.
     */
    String cacheDirectory

    /**
     * The name of the eclipse version. If not set, either {@link #linuxEclipseVersion} or
     * {@link #windowsEclipseVersion} will be used.
     */
    String eclipseVersion

    /**
     * The name of the unzipped eclipse distribution. This property is optional.
     */
    String eclipseArchiveName

    /**
     * The download url to the eclipse distribution. If not set, either {@link #linuxDownloadUrl} or
     * {@link #windowsDownloadUrl} will be used.
     */
    String eclipseDownloadUrl

    /**
     * The location of the plugins within the distribution. This property is optional.
     */
    String eclipsePluginsDirectory

    /**
     * The download url to the linux eclipse distribution.
     */
    String linuxDownloadUrl

    /**
     * The name of the linux eclipse version.
     */
    String linuxEclipseVersion

    /**
     * The download url to the windows eclipse distribution.
     */
    String windowsDownloadUrl

    /**
     * The name of the windows eclipse version.
     */
    String windowsEclipseVersion

    SeasideEclipseUpdateSiteExtension(Project project) {
        updateSiteArchiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "caches", "eclipse")
    }

    String getEclipseVersion() {
        if (eclipseVersion == null) {
            Preconditions.checkNotNull(linuxEclipseVersion, "linuxEclipseVersion must be defined!")
            Preconditions.checkNotNull(windowsEclipseVersion, "windowsEclipseVersion must be defined!")
            Preconditions.checkState(
                  OperatingSystem.current().isLinux() || OperatingSystem.current().isWindows(),
                  "supported operating systems are Linux and Windows!"
            )

            eclipseVersion = OperatingSystem.current().isLinux() ? linuxEclipseVersion : windowsEclipseVersion
        }

        return eclipseVersion
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
            eclipsePluginsDirectory = Paths.get(cacheDirectory, getEclipseVersion(), "plugins")
        }

        return eclipsePluginsDirectory
    }

    String getEclipseArchiveName() {
        if (eclipseArchiveName == null) {
            eclipseArchiveName = Paths.get(cacheDirectory, "${getEclipseVersion()}.zip")
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
