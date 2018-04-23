package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class SeasideReleasePluginFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        );
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Test
    void doesReleaseWhenSnapshotIsSpecified() {
        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "releaseDryRun")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "releaseDryRun")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "releaseDryRun")
    }

    @Test
    void doesFailWhenReleasingAndSnapshotIsNotSpecified() {
        FileUtils.copyFile(
            Paths.get(sourceDirectoryWithTheTestProject().toString(), "build-without-snapshot.gradle").toFile(),
            Paths.get(pathToTheDestinationProjectDirectory().toString(), "build.gradle").toFile()
        )

        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .buildAndFail()

        Assert.assertNull(result.task(":clean"))
        Assert.assertNull(result.task(":build"))
        Assert.assertNull(result.task(":releaseDryRun"))
    }

    @Test
    void doesNotFailBuildWhenSnapshotMissingAndReleaseTaskNotStated() {
        FileUtils.copyFile(
            Paths.get(sourceDirectoryWithTheTestProject().toString(), "build-without-snapshot.gradle").toFile(),
            Paths.get(pathToTheDestinationProjectDirectory().toString(), "build.gradle").toFile()
        )

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "install")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "install")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "install")
    }

    @Test
    void doesNotFailBuildWhenSnapshotPresentAndReleaseTaskNotStated() {
        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "install")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "install")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "install")
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "sealion-java-hello-world"
        );
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "release", "sealion-java-hello-world"
        );
    }
}
