package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import org.gradle.api.Project

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
     * The directory in which eclipse plugins are stored. This property is optional.
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

    /**
     * Create an instance of the SeasideEclipseUpdateSiteExtension
     * @param project the project on which to create the extension
     */
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
