package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.test.TestingUtilities
import com.ngc.seaside.gradle.util.TaskResolver
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class SeasideReleasePluginIT {
    private static final String BUILD_GRADLE_TEST_VERSION_NUMBER = "1.2.3-SNAPSHOT"
    private static final String VERSIONS_GRADLE_TEST_VERSION_NUMBER = "1.2.4-SNAPSHOT"

    private File projectDir
    private Project project
    private TaskResolver resolver
    private SeasideReleasePlugin plugin

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Test
    void doesApplyPlugin() {
        makeTheVersionResolverUseTheDefaultVersionFile()
        applyThePlugin()

        Assert.assertEquals(BUILD_GRADLE_TEST_VERSION_NUMBER, project.version.toString())

        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))

        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
    }

    @Test
    void doesApplyPluginFromDifferentVersionFile() {
        applyThePlugin()

        Assert.assertEquals(VERSIONS_GRADLE_TEST_VERSION_NUMBER, project.version.toString())

        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))

        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
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

    private void makeTheVersionResolverUseTheDefaultVersionFile() {
        Paths.get(projectDir.toString(), "versions.gradle").toFile().delete()
    }

    private void applyThePlugin() {
        plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        project.extensions
              .findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME)
              .uploadArtifacts = false

        project.evaluate()
        resolver = new TaskResolver(project)
    }
}
