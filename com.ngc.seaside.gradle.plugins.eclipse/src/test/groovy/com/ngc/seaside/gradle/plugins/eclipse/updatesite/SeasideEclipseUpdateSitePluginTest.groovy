package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.TaskResolver

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideEclipseUpdateSitePluginTest {
    private static final String TEST_LINUX_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-linux-gtk-x86_64"
    private static final String TEST_WINDOWS_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-win32-x86_64"
    private static final String TEST_LINUX_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/$TEST_LINUX_ECLIPSE_VERSION"
    private static final String TEST_WINDOWS_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/$TEST_WINDOWS_ECLIPSE_VERSION"

    private SeasideEclipseUpdateSitePlugin plugin
    private TaskResolver resolver
    private Project project
    private File projectDir
    private List<String> taskNames = [
          SeasideEclipseUpdateSitePlugin.ECLIPSE_DOWNLOAD_ECLIPSE_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_UNZIP_ECLIPSE_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_FEATURES_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_CUSTOM_PLUGINS_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_METADATA_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME,
    ]

    @Before
    void before() {
        project = ProjectBuilder.builder().withName('test').build()
        project.setProperty('version', '1.2.3')
        project.setProperty('group', 'test')
        project.apply plugin: 'com.ngc.seaside.eclipse.updatesite'
        resolver = new TaskResolver(project)
    }

    @Test
    void tasksExist() {
        verifyTasksExistOnThePlugin()
        Assert.assertNotNull(
              "clean task does not exist!",
              project.tasks.getByName("clean")
        )
        Assert.assertTrue(
              "build task does not depend on $SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME",
              project.tasks
                     .getByName("build")
                     .dependsOn
                     .contains(project.tasks.getByName(SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_UPDATE_SITE_ZIP_TASK_NAME))
        )
    }

    @Test
    void configurationsExist() {
        def configurationNames = ['features', 'eclipsePlugins', 'customPlugins']
        configurationNames.forEach({ name ->
            Assert.assertTrue(
                  "configuration $name do not exist!",
                  project.configurations
                         .stream()
                         .map({ config -> config.name })
                         .toArray()
                         .contains(name)
            )

            if (name.endsWith('Plugins')) {
                Assert.assertFalse(
                      "$name should set transitive to false!",
                      project.configurations.getByName(name).transitive
                )
            }
        })
    }

    @Test
    void repositoryExists() {
        project.afterEvaluate {
            Assert.assertFalse(
                  "there should be a configured flatDir repository!",
                  project.repositories.empty || project.repositories.get(0).name != 'flatDir'
            )

            def eclipsePluginsDir = project.plugins
                                           .getPlugin(SeasideEclipseUpdateSitePlugin.class)
                                           .eclipseProperties
                                           .eclipsePluginsDirectory
            def dirs = project.repositories.getByName('flatDir').properties.get('dirs').collect()
            Assert.assertFalse(
                  "the flatDir repository dirs should point to $eclipsePluginsDir",
                  dirs.empty || dirs[0].toString() != eclipsePluginsDir
            )
        }
    }

    @Test
    void extensionExists() {
        Assert.assertNotNull(
              "eclipse update extension does not exist!",
              project.extensions.findByName(SeasideEclipseUpdateSitePlugin.ECLIPSE_UPDATE_SITE_EXTENSION_NAME)
        )
    }

    private void verifyTasksExistOnThePlugin() {
        taskNames.each { taskName ->
            Assert.assertNotNull(
                  "$taskName task does not exist!",
                  resolver.findTask(taskName)
            )
        }
    }
}
