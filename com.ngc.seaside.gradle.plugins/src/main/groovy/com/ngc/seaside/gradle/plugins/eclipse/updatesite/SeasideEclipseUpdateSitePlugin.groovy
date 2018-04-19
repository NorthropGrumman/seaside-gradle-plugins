package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite"
    public static final String ECLIPSE_COPY_FEATURES_TASK_NAME = "copyFeatures"
    public static final String ECLIPSE_COPY_SD_PLUGINS_TASK_NAME = "copySdPlugins"
    public static final String ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME = "copyEclipsePlugins"
    public static final String ECLIPSE_CREATE_METADATA_TASK_NAME = "createMetadata"
    public static final String ECLIPSE_CREATE_ZIP_TASK_NAME = "createZip"

    @Override
    void doApply(Project project) {
        project.configure(project) {
            project.repositories {
                flatDir {
                    // TODO(Cameron): Replace with extension property
                    dirs "/tmp"
                }
            }

            project.configurations {
                features
                sdPlugins {
                    transitive = false
                }
                eclipsePlugins {
                    transitive = false
                }
            }

            createTasks(project)

            defaultTasks = ["build"]
        }
    }

    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_COPY_FEATURES_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_FEATURES_TASK_NAME")

        project.task(
              ECLIPSE_COPY_SD_PLUGINS_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_SD_PLUGINS_TASK_NAME")

        project.task(
              ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME")

        project.task(
              ECLIPSE_CREATE_METADATA_TASK_NAME,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_CREATE_METADATA_TASK_NAME",
              dependsOn: [
                    ECLIPSE_COPY_FEATURES_TASK_NAME,
                    ECLIPSE_COPY_SD_PLUGINS_TASK_NAME,
                    ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
              ])

        def task = project.task(
              ECLIPSE_CREATE_ZIP_TASK_NAME,
              type: Zip,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_CREATE_ZIP_TASK_NAME",
              dependsOn: ECLIPSE_CREATE_METADATA_TASK_NAME)

        project.task("clean") {
            project.delete(project.buildDir)
        }

        project.task("build").dependsOn(task)
    }
}
