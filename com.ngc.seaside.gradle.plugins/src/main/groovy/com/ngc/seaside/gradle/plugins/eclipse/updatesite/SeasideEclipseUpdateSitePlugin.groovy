package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.CreateMetadataTask
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.DownloadEclipseTask
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.UnzipEclipseTask
import com.ngc.seaside.gradle.util.EclipsePlugins
import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Paths

class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite"
    public static final String ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME = "downloadEclipse"
    public static final String ECLIPSE_UNZIP_ECLIPSE_TASK_NAME = "unzipEclipse"
    public static final String ECLIPSE_COPY_FEATURES_TASK_NAME = "copyFeatures"
    public static final String ECLIPSE_COPY_SD_PLUGINS_TASK_NAME = "copySdPlugins"
    public static final String ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME = "copyEclipsePlugins"
    public static final String ECLIPSE_CREATE_METADATA_TASK_NAME = "createMetadata"
    public static final String ECLIPSE_CREATE_ZIP_TASK_NAME = "createZip"

    public String updateSiteArchiveName
    public String cacheDirectory
    public String linuxDownloadUrl
    public String linuxEclipseVersion
    public String windowsDownloadUrl
    public String windowsEclipseVersion
    public String eclipseVersion
    public String eclipseArchiveName
    public String eclipseDownloadUrl
    public String eclipsePluginsDirectory

    private SeasideEclipseUpdateSiteExtension extension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            createExtension(project)

            project.afterEvaluate {
                project.repositories {
                    flatDir {
                        dirs extension.getEclipsePluginsDirectory()
                    }
                }

                configureTasks(project)
            }

            project.configurations {
                features
                customPlugins {
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
        setExtensionProperties(project)
    }

    private void setExtensionProperties(Project project) {
        extension.updateSiteArchiveName = updateSiteArchiveName ?: extension.updateSiteArchiveName
        extension.cacheDirectory = cacheDirectory ?: extension.cacheDirectory
        extension.linuxDownloadUrl = linuxDownloadUrl ?: extension.linuxDownloadUrl
        extension.linuxEclipseVersion = linuxEclipseVersion ?: extension.linuxEclipseVersion
        extension.windowsDownloadUrl = windowsDownloadUrl ?: extension.windowsDownloadUrl
        extension.windowsEclipseVersion = windowsEclipseVersion ?: extension.windowsEclipseVersion

        project.afterEvaluate {
            extension.eclipseVersion = eclipseVersion ?: extension.eclipseVersion
            extension.eclipseArchiveName = eclipseArchiveName ?: extension.eclipseArchiveName
            extension.eclipseDownloadUrl = eclipseDownloadUrl ?: extension.eclipseDownloadUrl
            extension.eclipsePluginsDirectory = eclipsePluginsDirectory ?: extension.eclipsePluginsDirectory
        }
    }

    private void configureTasks(Project project) {
        project.getTasks().getByName(ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME) {
            eclipseArchiveName = extension.eclipseArchiveName
            eclipseDownloadUrl = extension.eclipseDownloadUrl
        }
    }


    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME,
              type: DownloadEclipseTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME")

        project.task(
              ECLIPSE_UNZIP_ECLIPSE_TASK_NAME,
              type: UnzipEclipseTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_UNZIP_ECLIPSE_TASK_NAME",
              dependsOn: ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME)

        project.task(
              ECLIPSE_COPY_FEATURES_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_FEATURES_TASK_NAME") {
            from project.configurations.features
            into Paths.get(project.buildDir.absolutePath, "updatesite", "features")
            rename { String name ->
                EclipsePlugins.makeEclipseCompliantJarFileName(name)
            }
        }

        project.task(
              ECLIPSE_COPY_SD_PLUGINS_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_SD_PLUGINS_TASK_NAME") {
            from project.configurations.customPlugins {
                rename { String name ->
                    def artifacts = project.configurations.customPlugins.resolvedConfiguration.resolvedArtifacts
                    def artifact = artifacts.find { it.file.name == name }
                    def osgiVersion = Versions.makeOsgiCompliantVersion("${artifact.moduleVersion.id.version}")
                    "${artifact.moduleVersion.id.group}.${artifact.name}_${osgiVersion}.${artifact.extension}"
                }
            }
            into Paths.get(project.buildDir.absolutePath, "updatesite", "plugins")
        }

        project.task(
              ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME",
              dependsOn: ECLIPSE_UNZIP_ECLIPSE_TASK_NAME) {
            from project.configurations.eclipsePlugins
            into Paths.get(project.buildDir.absolutePath, "updatesite", "plugins")
        }

        project.task(
              ECLIPSE_CREATE_METADATA_TASK_NAME,
              type: CreateMetadataTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_CREATE_METADATA_TASK_NAME",
              dependsOn: [
                    ECLIPSE_UNZIP_ECLIPSE_TASK_NAME,
                    ECLIPSE_COPY_FEATURES_TASK_NAME,
                    ECLIPSE_COPY_SD_PLUGINS_TASK_NAME,
                    ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
              ])

        project.task(
              ECLIPSE_CREATE_ZIP_TASK_NAME,
              type: Zip,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "description of: $ECLIPSE_CREATE_ZIP_TASK_NAME",
              dependsOn: ECLIPSE_CREATE_METADATA_TASK_NAME) {
            from Paths.get(project.buildDir.absolutePath, "updatesite")
            destinationDir = project.buildDir
            archiveName = project.extensions.getByType(SeasideEclipseUpdateSiteExtension.class).updateSiteArchiveName
        }

        project.task("clean") {
            project.delete(project.buildDir)
        }

        project.task("build").dependsOn(project.tasks.getByName(ECLIPSE_CREATE_ZIP_TASK_NAME))
    }
}
