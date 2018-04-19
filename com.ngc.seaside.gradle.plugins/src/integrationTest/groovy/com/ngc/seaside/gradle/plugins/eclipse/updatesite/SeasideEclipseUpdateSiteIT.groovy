package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.TaskResolver
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class SeasideEclipseUpdateSiteIT {
    private SeasideEclipseUpdateSitePlugin plugin
    private TaskResolver resolver
    private Project project
    private File projectDir
    private List<String> taskNames = [
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_FEATURES_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_SD_PLUGINS_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_COPY_ECLIPSE_PLUGINS_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_METADATA_TASK_NAME,
          SeasideEclipseUpdateSitePlugin.ECLIPSE_CREATE_ZIP_TASK_NAME,
    ]

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
              sourceDirectoryWithTheTestProject(),
              pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
        plugin = new SeasideEclipseUpdateSitePlugin()
        plugin.apply(project)
        resolver = new TaskResolver(project)
    }

    @Test
    void tasksExist() {
        verifyTasksExistOnThePlugin()
    }

    @Test
    void configurationsExist() {
        def configurationNames = ['features', 'eclipsePlugins', 'sdPlugins']
        configurationNames.forEach({ name ->
            Assert.assertTrue(
                  "configuration $name do not exist!",
                  project.configurations
                        .stream()
                        .map({ config -> config.name })
                        .toArray()
                        .contains(name)
            )
        })
    }

    @Ignore
    @Test
    void extensionExists() {
        Assert.assertNotNull(
              "eclipse update extension does not exist!",
              project.extensions.findByName(SeasideEclipseUpdateSitePlugin.ECLIPSE_UPDATE_SITE_EXTENSION_NAME)
        )
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
              "src", "integrationTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
              "build", "integrationTest", "eclipse", "updatesite", "sealion-java-hello-world"
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
