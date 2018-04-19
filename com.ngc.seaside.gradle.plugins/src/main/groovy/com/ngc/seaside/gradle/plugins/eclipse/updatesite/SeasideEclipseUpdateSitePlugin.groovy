package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite"
    public static final String ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME = "downloadEclipse"
    public static final String ECLIPSE_COPY_FEATURES_TASK_NAME = "copyFeatures"
    public static final String ECLIPSE_COPY_SD_PLUGINS_TASK_NAME = "copySdPlugins"
    public static final String ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME = "copyEclipsePlugins"
    public static final String ECLIPSE_CREATE_METADATA_TASK_NAME = "createMetadata"
    public static final String ECLIPSE_CREATE_ZIP_TASK_NAME = "createZip"

    public String archiveName
    public String cacheDirectory
    public String eclipseVersion
    public String eclipsePluginsDirectory
    public String linuxDownloadUrl
    public String windowsDownloadUrl

    private SeasideEclipseUpdateSiteExtension extension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            createExtension(project)

            project.repositories {
                flatDir {
                    dirs extension.eclipsePluginsDirectory
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

            project.defaultTasks = ["build"]
        }
    }

    private void createExtension(Project project) {
        extension = project.extensions
                           .create(ECLIPSE_UPDATE_SITE_EXTENSION_NAME, SeasideEclipseUpdateSiteExtension, project)
        setExtensionProperties()
    }

    private void setExtensionProperties() {
        extension.archiveName = archiveName ?: extension.archiveName
        extension.cacheDirectory = cacheDirectory ?: extension.cacheDirectory
        extension.eclipseVersion = eclipseVersion ?: extension.eclipseVersion
        extension.eclipsePluginsDirectory = eclipsePluginsDirectory ?: extension.eclipsePluginsDirectory
        extension.linuxDownloadUrl = linuxDownloadUrl ?: extension.linuxDownloadUrl
        extension.windowsDownloadUrl = windowsDownloadUrl ?: extension.windowsDownloadUrl
    }


    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME")

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
