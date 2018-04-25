package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DownloadEclipseTask extends DefaultTask {
    @TaskAction
    void downloadEclipse() {
        def extension = project.extensions.getByType(SeasideEclipseUpdateSiteExtension.class)

        def destFile = project.file(extension.eclipseArchiveName)
        destFile.getParentFile().mkdirs()

        if (!destFile.exists()) {
            println "Downloading Eclipse SDK from ${extension.eclipseDownloadUrl}..."
            new URL(extension.eclipseDownloadUrl).withInputStream { is ->
                destFile.withOutputStream { it << is }
            }
        }
    }
}
