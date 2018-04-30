package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

/**
 * Task for creating the metadata for the Eclipse update site.
 */
class CreateMetadataTask extends DefaultTask {
    /**
     * The cached directory for the unzipped Eclipse distribution.
     */
    String cacheDirectory

    /**
     * The name of the Eclipse version.
     */
    String eclipseVersion

    /**
     * Create the metadata for the Eclipse update site.
     */
    @TaskAction
    void createMetadata() {
        def fileExtension = OperatingSystem.current().isLinux() ? "" : ".exe"
        def updateSiteDir = project.file(Paths.get(project.buildDir.absolutePath, "updatesite"))
        project.exec {
            commandLine(
                  Paths.get(cacheDirectory, eclipseVersion, "eclipse$fileExtension"),
                  '-nosplash',
                  '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher',
                  '-compress',
                  '-metadataRepository', updateSiteDir.toURI().toURL(),
                  '-artifactRepository', updateSiteDir.toURI().toURL(),
                  '-source', updateSiteDir
            )
        }
    }
}
