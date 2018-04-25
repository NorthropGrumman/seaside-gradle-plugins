package com.ngc.seaside.gradle.tasks.eclipse.updatesite

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem

import java.nio.file.Paths

class CreateMetadataTask extends DefaultTask {
    @TaskAction
    void createMetadata() {
        def extension = project.extensions.getByType(SeasideEclipseUpdateSiteExtension.class)
        def executable = Paths.get(
              extension.cacheDirectory,
              extension.eclipseVersion,
              "eclipse" + (OperatingSystem.current().isLinux() ? "" : ".exe")
        )
        def updateSiteDir = project.file(Paths.get(project.buildDir.absolutePath, "updatesite"))
        project.exec {
            commandLine(
                  executable,
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
