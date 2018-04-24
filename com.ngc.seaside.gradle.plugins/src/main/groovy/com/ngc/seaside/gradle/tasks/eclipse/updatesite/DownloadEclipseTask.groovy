package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

class DownloadEclipseTask extends DefaultTask {
    private SeasideEclipseUpdateSiteExtension extension

    @TaskAction
    void downloadEclipse() {
        extension = project.extensions.getByType(SeasideEclipseUpdateSiteExtension.class)
        def destFile = project.file(extension.getEclipseArchiveName())
        destFile.getParentFile().mkdirs()

        if (!destFile.exists()) {
            project.logger.info("Downloading Eclipse SDK from ${extension.getEclipseDownloadUrl()}...")
            new URL(extension.getEclipseDownloadUrl()).withInputStream { is ->
                destFile.withOutputStream { it << is }
            }
        }
    }
}
