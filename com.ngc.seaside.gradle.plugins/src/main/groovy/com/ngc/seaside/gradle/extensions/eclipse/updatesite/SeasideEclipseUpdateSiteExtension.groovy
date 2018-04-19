package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class SeasideEclipseUpdateSiteExtension {
    public static final String OS_SPECIFIER = OperatingSystem.current().isLinux() ? "linux-gtk" : "win32"

    public String archiveName
    public String cacheDirectory
    public String eclipseVersion
    public String eclipsePluginsDirectory

    public String linuxDownloadUrl
    public String windowsDownloadUrl

    SeasideEclipseUpdateSiteExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.zip"
        cacheDirectory = Paths.get(project.gradle.gradleUserHomeDir.absolutePath, "eclipse").toString()
        eclipseVersion = "eclipse-dsl-oxygen-2-$OS_SPECIFIER-x86_64"
        eclipsePluginsDirectory = Paths.get(cacheDirectory, eclipseVersion, "plugins").toString()
    }
}
