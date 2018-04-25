package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UnzipEclipseTask extends DefaultTask {
    @TaskAction
    void unzipEclipse() {
        def extension = project.extensions.getByType(SeasideEclipseUpdateSiteExtension.class)

        if (!project.file(extension.eclipseArchiveName - ".zip").exists()) {
            project.copy {
                from project.zipTree(project.file(extension.eclipseArchiveName))
                into extension.cacheDirectory
            }
        }
    }
}
