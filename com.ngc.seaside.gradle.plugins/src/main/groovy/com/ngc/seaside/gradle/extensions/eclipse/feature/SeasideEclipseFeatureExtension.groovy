package com.ngc.seaside.gradle.extensions.eclipse.feature

import org.gradle.api.Project

/**
 * Extension for the seaside eclipse feature plugin.
 */
class SeasideEclipseFeatureExtension {
    /**
     * The archive name of the feature jar. By default this is {@code group.artifact-version.jar}
     */
    String archiveName

    /**
     * Create an instance of the SeasideEclipseFeatureExtension
     * @param project the project on which to create the extension
     */
    SeasideEclipseFeatureExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.jar"
    }
}
