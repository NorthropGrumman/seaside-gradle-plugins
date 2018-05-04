package com.ngc.seaside.gradle.util.eclipse

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

/**
 * Utility class for providing values to the Eclipse updatesite plugin based on OS, etc.
 */
class EclipsePropertyUtil {
    private static final boolean IS_LINUX = OperatingSystem.current().isLinux()
    private static final boolean IS_WINDOWS = OperatingSystem.current().isWindows()

    private SeasideEclipseUpdateSiteExtension extension

    /**
     * Create an instance of EclipsePropertyUtil based on the SeasideEclipseUpdateSiteExtension. This class requires the
     * extension to already have the required values set on it.
     *
     * @param extension the pre-populated extension for the Eclipse updatesite plugin
     */
    EclipsePropertyUtil(SeasideEclipseUpdateSiteExtension extension) {
        Preconditions.checkNotNull(extension.linuxEclipseVersion, "linuxEclipseVersion not defined on extension!")
        Preconditions.checkNotNull(extension.windowsEclipseVersion, "windowsEclipseVersion not defined on extension!")
        Preconditions.checkNotNull(extension.linuxDownloadUrl, "linuxDownloadUrl not defined on extension!")
        Preconditions.checkNotNull(extension.windowsDownloadUrl, "windowsDownloadUrl not defined on extension!")
        Preconditions.checkState(IS_LINUX || IS_WINDOWS, "supported operating systems are Linux and Windows!")

        this.extension = extension
    }

    /**
     * Get the OS-dependent eclipse version string.
     * @return the OS-dependent eclipse version string
     */
    String getEclipseVersion() {
        return IS_LINUX ? extension.linuxEclipseVersion : extension.windowsEclipseVersion
    }

    /**
     * Get the OS-dependent eclipse download url.
     * @return the OS-dependent eclipse download url
     */
    String getEclipseDownloadUrl() {
        return IS_LINUX ? extension.linuxDownloadUrl : extension.windowsDownloadUrl
    }

    /**
     * Get the location where eclipse plugins are stored.
     * @return the eclipse plugins directory location
     */
    String getEclipsePluginsDirectory() {
        return extension.eclipsePluginsDirectory ?: Paths.get(extension.cacheDirectory, eclipseVersion, "plugins")
    }

    /**
     * Get the name of the eclipse SDK archive.
     * @return the eclipse SDK archive name
     */
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
