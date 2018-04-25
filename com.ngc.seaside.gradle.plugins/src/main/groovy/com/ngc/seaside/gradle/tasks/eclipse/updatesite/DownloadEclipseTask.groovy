package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DownloadEclipseTask extends DefaultTask {
    String eclipseArchiveName
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
