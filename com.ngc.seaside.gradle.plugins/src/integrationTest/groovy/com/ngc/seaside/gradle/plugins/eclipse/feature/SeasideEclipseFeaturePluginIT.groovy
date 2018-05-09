package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.util.TaskResolver
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideEclipseFeaturePluginIT {
    private SeasideEclipseFeaturePlugin plugin
    private TaskResolver resolver
    private Project project
    private File projectDir
    private List<String> taskNames = [
          SeasideEclipseFeaturePlugin.ECLIPSE_CREATE_JAR_TASK_NAME,
          SeasideEclipseFeaturePlugin.ECLIPSE_COPY_FEATURE_FILE_TASK_NAME,
    ]

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
              sourceDirectoryWithTheTestProject(),
              pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
        plugin = new SeasideEclipseFeaturePlugin()
        plugin.apply(project)
        resolver = new TaskResolver(project)
    }

    @Test
    void tasksExist() {
        verifyTasksExistOnThePlugin()
    }

    @Test
    void extensionExists() {
        Assert.assertNotNull(
              "eclipse extension does not exist!",
              project.extensions.findByName(SeasideEclipseFeaturePlugin.ECLIPSE_FEATURE_EXTENSION_NAME)
        )
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
              "src", "integrationTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
              "build", "integrationTest", "eclipse", "feature", "sealion-java-hello-world"
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
