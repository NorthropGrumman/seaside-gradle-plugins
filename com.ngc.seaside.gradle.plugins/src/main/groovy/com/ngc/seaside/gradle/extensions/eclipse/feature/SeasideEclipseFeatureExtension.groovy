package com.ngc.seaside.gradle.extensions.eclipse.feature

import org.gradle.api.Project

class SeasideEclipseFeatureExtension {
    public String archiveName

    SeasideEclipseFeatureExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.jar"
    }
}
