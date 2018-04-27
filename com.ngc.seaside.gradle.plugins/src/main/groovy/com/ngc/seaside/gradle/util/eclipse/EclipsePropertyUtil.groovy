package com.ngc.seaside.gradle.util.eclipse

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class EclipsePropertyUtil {
    private static final boolean IS_LINUX = OperatingSystem.current().isLinux()
    private static final boolean IS_WINDOWS = OperatingSystem.current().isWindows()

    private SeasideEclipseUpdateSiteExtension extension

    EclipsePropertyUtil(SeasideEclipseUpdateSiteExtension extension) {
        Preconditions.checkNotNull(extension.linuxEclipseVersion, "linuxEclipseVersion not defined on extension!")
        Preconditions.checkNotNull(extension.windowsEclipseVersion, "windowsEclipseVersion not defined on extension!")
        Preconditions.checkNotNull(extension.linuxDownloadUrl, "linuxDownloadUrl not defined on extension!")
        Preconditions.checkNotNull(extension.windowsDownloadUrl, "windowsDownloadUrl not defined on extension!")
        Preconditions.checkState(IS_LINUX || IS_WINDOWS, "supported operating systems are Linux and Windows!")

        this.extension = extension
    }

    String getEclipseVersion() {
        return IS_LINUX ? extension.linuxEclipseVersion : extension.windowsEclipseVersion
    }

    String getEclipseDownloadUrl() {
        return IS_LINUX ? extension.linuxDownloadUrl : extension.windowsDownloadUrl
    }

    String getEclipsePluginsDirectory() {
        return Paths.get(extension.cacheDirectory, eclipseVersion, "plugins")
    }

    String getEclipseArchiveName() {
        return Paths.get(extension.cacheDirectory, "${eclipseVersion}.zip")
    }

    @Override
    String toString() {
        return "EclipsePropertyUtil[" +
               "\n\teclipseVersion=" + eclipseVersion +
               "\n\teclipseDownloadUrl=" + eclipseDownloadUrl +
               "\n\teclipsePluginsDirectory=" + eclipsePluginsDirectory +
               "\n\teclipseArchiveName=" + eclipseArchiveName +
               "\n]"
    }
}
