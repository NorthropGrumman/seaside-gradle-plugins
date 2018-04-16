package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.SeasideEclipseExtension
import org.gradle.api.Project

class SeasideEclipseFeaturePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_EXTENSION_NAME = "seasideEclipse"
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_CREATE_JAR_TASK_NAME = "createJar"
    public static final String ECLIPSE_FILTER_FEATURE_FILE_TASK_NAME = "filterFeatureFile"
    public static final String ECLIPSE_COPY_FEATURE_FILE_TASK_NAME = "copyFeatureFile"

    private SeasideEclipseExtension extension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            extension = project.extensions.create(ECLIPSE_EXTENSION_NAME, SeasideEclipseExtension, project)
            createTasks(project)
        }
    }

    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_CREATE_JAR_TASK_NAME,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Create the eclipse feature file jar",
              dependsOn: ECLIPSE_COPY_FEATURE_FILE_TASK_NAME
        )

        project.task(
              ECLIPSE_FILTER_FEATURE_FILE_TASK_NAME,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Perform property expansion on the feature file"
        )

        project.task(
              ECLIPSE_COPY_FEATURE_FILE_TASK_NAME,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Copy the feature file to the correct location",
              dependsOn: ECLIPSE_FILTER_FEATURE_FILE_TASK_NAME
        )
    }
}
