package com.ngc.seaside.gradle.plugins.eclipse.updatesite

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

    /**
     * Download the Eclipse SDK from the specified URL, if it doesn't already exist in the specified location.
     */
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