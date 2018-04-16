package com.ngc.seaside.gradle.extensions.eclipse.feature

import org.gradle.api.Project

class SeasideEclipseExtension {
    public String archiveName

    SeasideEclipseExtension(Project project) {
        archiveName = "${project.group}.${project.name}-${project.version}.zip"
    }
}
