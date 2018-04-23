package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import com.google.common.base.Preconditions
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class SeasideEclipseUpdateSiteExtension {
    public String archiveName
    public String cacheDirectory
    public String eclipsePluginsDirectory

    public String linuxDownloadUrl
    public String linuxEclipseVersion
    public String windowsDownloadUrl
    public String windowsEclipseVersion

    SeasideEclipseUpdateSiteExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "eclipse").toString()
        eclipsePluginsDirectory = Paths.get(cacheDirectory, "plugins").toString()
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
}
