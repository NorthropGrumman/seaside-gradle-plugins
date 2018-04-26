package com.ngc.seaside.gradle.extensions.eclipse.updatesite

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
     * The download url to the linux eclipse distribution.
     */
    String linuxDownloadUrl

    /**
     * The download url to the windows eclipse distribution.
     */
    String windowsDownloadUrl

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
