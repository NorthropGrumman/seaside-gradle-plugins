package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.CreateMetadataTask
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.DownloadEclipseTask
import com.ngc.seaside.gradle.tasks.eclipse.updatesite.UnzipEclipseTask
import com.ngc.seaside.gradle.util.EclipsePlugins
import com.ngc.seaside.gradle.util.Versions
import com.ngc.seaside.gradle.util.eclipse.EclipsePropertyUtil
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Paths

/**
 * Plugin used for building the update site of an Eclipse plugin.
 *
 * <p> This plugin creates the {@value #ECLIPSE_UPDATE_SITE_EXTENSION_NAME} extension name that uses
 * {@link SeasideEclipseUpdateSiteExtension}. Projects that use this plugin must set the eclipse version and
 * download url in the extension (or both the windows and linux counterparts).
 *
 * <p> This plugin adds the following configurations for update site dependencies:
 * <ul>
 * <li>features - feature jars that should be included in the update site. Example:
 * <br> {@code features project(path: ':<project-name>.feature', configuration: 'feature')}
 * </li>
 * <li>sdPlugins - plugin projects that should be included in the update site. Example:
 * <br> {@code sdPlugins project(':<project-name>')}
 * <br> {@code sdPlugins project(':<project-name>.ide')}
 * <br> {@code sdPlugins project(':<project-name>.ui')}
 * </li>
 * <li> eclipsePlugins - eclipse plugin jars that should be included in the update site. These jars must exist in the
 * plugins folder of the {@link SeasideEclipseUpdateSiteExtension#getEclipseDownloadUrl eclipse download}. Example:
 * <br> {@code eclipsePlugins name: 'org.eclipse.xtext.lib_2.12.0.v20170518-0757'}
 * </li>
 * </ul>
 */
class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {
    /**
     * The eclipse task group name.
     */
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    /**
     * The eclipse updatesite extension name.
     */
    public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite"

    /**
     * The name of the task for downloading the eclipse SDK.
     */
    public static final String ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME = "downloadEclipse"

    /**
     * The name of the task for unzipping the eclipse SDK.
     */
    public static final String ECLIPSE_UNZIP_ECLIPSE_TASK_NAME = "unzipEclipse"

    /**
     * The name of the task for copying features to the update site.
     */
    public static final String ECLIPSE_COPY_FEATURES_TASK_NAME = "copyFeatures"

    /**
     * The name of the task for copying the custom plugins to the update site.
     */
    public static final String ECLIPSE_COPY_CUSTOM_PLUGINS_TASK_NAME = "copyCustomPlugins"

    /**
     * The name of the task for copying the eclipse plugins to the update site.
     */
    public static final String ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME = "copyEclipsePlugins"

    /**
     * The name of the task for creating the eclipse metadata for the update site.
     */
    public static final String ECLIPSE_CREATE_METADATA_TASK_NAME = "createMetadata"

    /**
     * The name of the task for creating the update site zip file.
     */
    public static final String ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME = "createZip"

    /**
     * The name of the update site archive file.
     */
    public String updateSiteArchiveName

    /**
     * The location of the cache directory.
     */
    public String cacheDirectory

    /**
     * The download url from which to download the eclipse SDK for linux.
     */
    public String linuxDownloadUrl

    /**
     * The version string for identifying eclipse on the filesystem in linux.
     */
    public String linuxEclipseVersion

    /**
     * The download url from which to download the eclipse SDK for windows.
     */
    public String windowsDownloadUrl

    /**
     * The version string for identifying eclipse on the filesystem in windows.
     */
    public String windowsEclipseVersion

    protected EclipsePropertyUtil eclipseProperties

    private SeasideEclipseUpdateSiteExtension extension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            createExtension(project)

            project.afterEvaluate {
                eclipseProperties = new EclipsePropertyUtil(extension)

                project.repositories {
                    flatDir {
                        dirs eclipseProperties.eclipsePluginsDirectory
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
        setExtensionProperties()
    }

    private void setExtensionProperties() {
        extension.updateSiteArchiveName = updateSiteArchiveName ?: extension.updateSiteArchiveName
        extension.cacheDirectory = cacheDirectory ?: extension.cacheDirectory
        extension.linuxDownloadUrl = linuxDownloadUrl ?: extension.linuxDownloadUrl
        extension.linuxEclipseVersion = linuxEclipseVersion ?: extension.linuxEclipseVersion
        extension.windowsDownloadUrl = windowsDownloadUrl ?: extension.windowsDownloadUrl
        extension.windowsEclipseVersion = windowsEclipseVersion ?: extension.windowsEclipseVersion
    }

    private void configureTasks(Project project) {
        project.getTasks().getByName(ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME) {
            eclipseArchiveName = eclipseProperties.eclipseArchiveName
            eclipseDownloadUrl = eclipseProperties.eclipseDownloadUrl
        }

        project.getTasks().getByName(ECLIPSE_UNZIP_ECLIPSE_TASK_NAME) {
            cacheDirectory = this.extension.cacheDirectory
            eclipseArchiveName = eclipseProperties.eclipseArchiveName
        }

        project.getTasks().getByName(ECLIPSE_CREATE_METADATA_TASK_NAME) {
            cacheDirectory = this.extension.cacheDirectory
            eclipseVersion = eclipseProperties.eclipseVersion
        }

        project.getTasks().getByName(ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME) {
            from Paths.get(project.buildDir.absolutePath, "updatesite")
            destinationDir = project.buildDir
            archiveName = this.extension.updateSiteArchiveName
        }
    }

    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME,
              type: DownloadEclipseTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Download the Eclipse SDK")

        project.task(
              ECLIPSE_UNZIP_ECLIPSE_TASK_NAME,
              type: UnzipEclipseTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Unzip the Eclipse SDK",
              dependsOn: ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME)

        project.task(
              ECLIPSE_COPY_FEATURES_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Copy the features jar to include it in the update site") {
            from project.configurations.features
            into Paths.get(project.buildDir.absolutePath, "updatesite", "features")
            rename { String name ->
                EclipsePlugins.makeEclipseCompliantJarFileName(name)
            }
        }

        project.task(
              ECLIPSE_COPY_CUSTOM_PLUGINS_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Copy the custom plugins to include them in the update site") {
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
              description: "Copy the Eclipse plugins to include them in the update site",
              dependsOn: ECLIPSE_UNZIP_ECLIPSE_TASK_NAME) {
            from project.configurations.eclipsePlugins
            into Paths.get(project.buildDir.absolutePath, "updatesite", "plugins")
        }

        project.task(
              ECLIPSE_CREATE_METADATA_TASK_NAME,
              type: CreateMetadataTask,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Create metadata for the update site",
              dependsOn: [
                    ECLIPSE_UNZIP_ECLIPSE_TASK_NAME,
                    ECLIPSE_COPY_FEATURES_TASK_NAME,
                    ECLIPSE_COPY_CUSTOM_PLUGINS_TASK_NAME,
                    ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
              ])

        project.task(
              ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME,
              type: Zip,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Archive the update site",
              dependsOn: ECLIPSE_CREATE_METADATA_TASK_NAME)

        project.task("clean") {
            project.delete(project.buildDir)
        }

        project.task("build").dependsOn(project.tasks.getByName(ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME))
    }
}
