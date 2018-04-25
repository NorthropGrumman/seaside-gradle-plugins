package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UnzipEclipseTask extends DefaultTask {
    String eclipseArchiveName
    String cacheDirectory

    @TaskAction
    void unzipEclipse() {
        if (!project.file(eclipseArchiveName - ".zip").exists()) {
            project.copy {
                from project.zipTree(project.file(eclipseArchiveName))
                into cacheDirectory
            }
        }
    }
}
