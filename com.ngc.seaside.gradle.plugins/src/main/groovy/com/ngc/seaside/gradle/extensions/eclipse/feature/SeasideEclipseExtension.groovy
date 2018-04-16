package com.ngc.seaside.gradle.extensions.eclipse

import org.gradle.api.Project

class SeasideEclipseExtension {
    private final String DEFAULT_ARCHIVE_NAME = "${project.group}.${project.name}-${project.version}.zip"

    private Project project

    SeasideEclipseExtension(Project project) {
        this.project = project
    }

    String archiveName = DEFAULT_ARCHIVE_NAME
}
