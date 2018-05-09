package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task for unzipping the downloaded Eclipse distribution.
 */
class UnzipEclipseTask extends DefaultTask {
    /**
     * The archive name of the Eclipse distribution.
     */
    String eclipseArchiveName

    /**
     * The cached directory for the unzipped Eclipse distribution.
     */
    String cacheDirectory

    /**
     * Unzip the Eclipse SDK zip file, if an unzipped version of it doesn't already exist.
     */
    @TaskAction
    void unzipEclipse() {
        if (!project.file(eclipseArchiveName - ".zip").exists()) {
            project.copy {
                from project.zipTree(project.file(eclipseArchiveName))
                into project.file(cacheDirectory)
            }
        }
    }
}
