package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task for downloading an Eclipse distribution.
 */
class DownloadEclipseTask extends DefaultTask {
    /**
     * The archive name of the Eclipse distribution.
     */
    String eclipseArchiveName

    /**
     * The download url to the Eclipse distribution.
     */
    String eclipseDownloadUrl

    @TaskAction
    void downloadEclipse() {
        def destFile = project.file(eclipseArchiveName)
        destFile.getParentFile().mkdirs()

        if (!destFile.exists()) {
            println "Downloading Eclipse SDK from ${eclipseDownloadUrl}..."
            new URL(eclipseDownloadUrl).withInputStream { is ->
                destFile.withOutputStream { it << is }
            }
        }
    }
}
