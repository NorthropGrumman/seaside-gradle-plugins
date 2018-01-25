package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.TestingUtilities
import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.util.TaskResolver
import javafx.concurrent.Task
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class SeasideReleaseMonoRepoPluginIT {
    private static final String BUILD_GRADLE_TEST_VERSION_NUMBER = "1.2.3-SNAPSHOT"
    private static final String VERSIONS_GRADLE_TEST_VERSION_NUMBER = "1.2.4-SNAPSHOT"

    private File projectDir
    private Project project
    private TaskResolver resolver
    private SeasideReleaseMonoRepoPlugin plugin
    private List<String> taskNames = [
            SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME,
            SeasideReleaseMonoRepoPlugin.RELEASE_CREATE_TAG_TASK_NAME,
            SeasideReleaseMonoRepoPlugin.RELEASE_PUSH_TASK_NAME,
            SeasideReleaseMonoRepoPlugin.RELEASE_BUMP_VERSION_TASK_NAME
    ]

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
                sourceDirectoryWithTheTestProject(),
                pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)

        plugin = new SeasideReleaseMonoRepoPlugin()
        plugin.apply(project)

        project.extensions
               .findByName(SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME)
               .uploadArtifacts = false
        resolver = new TaskResolver(project)
    }

    @Test
    void doesApplyPlugin() {
        project.evaluate()

        Assert.assertEquals(BUILD_GRADLE_TEST_VERSION_NUMBER, project.version.toString())

        Assert.assertNotNull(project.extensions.findByName(SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME))

        checkForTask()
    }

    @Test
    void desApplyPluginUsingDifferentVersionFile() {
        project.extensions
                .findByName(AbstractProjectPlugin.VERSION_SETTINGS_CONVENTION_NAME)
                .versionFile = Paths.get(sourceDirectoryWithTheTestProject().toString(), "versions.gradle").toFile()

        project.evaluate()

        Assert.assertEquals(VERSIONS_GRADLE_TEST_VERSION_NUMBER, project.version.toString())

        Assert.assertNotNull(project.extensions.findByName(SeasideReleaseMonoRepoPlugin.RELEASE_MONO_EXTENSION_NAME))

        checkForTask()
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
                "src", "integrationTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
                "build", "integrationTest", "resources", "release", "sealion-java-hello-world"
        )
    }

    private void checkForTask() {
        taskNames.each { taskNames ->
            Assert.assertNotNull(resolver.findTask(taskNames))
        }
    }
}
