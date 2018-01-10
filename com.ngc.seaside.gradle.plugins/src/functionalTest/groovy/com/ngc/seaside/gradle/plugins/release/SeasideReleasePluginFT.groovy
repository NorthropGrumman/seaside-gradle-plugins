package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideReleasePluginFT {

    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())
        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/release/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesReleaseWhenSnapshotIsSpecified() {
        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.bonjourlemonde:releaseDryRun").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.helloworld:releaseDryRun").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.holamundo:releaseDryRun").getOutcome())
    }

    @Test
    void doesFailWhenReleasingAndSnapshotIsNotSpecified() {
        FileUtils.copyFile(
                Paths.get("src/functionalTest/resources/sealion-java-hello-world/build-without-snapshot.gradle").
                        toFile(),
                Paths.get("build/functionalTest/release/sealion-java-hello-world/build.gradle").toFile()
        )

        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = GradleRunner.create()
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
                Paths.get("src/functionalTest/resources/sealion-java-hello-world/build-without-snapshot.gradle").
                        toFile(),
                Paths.get("build/functionalTest/release/sealion-java-hello-world/build.gradle").toFile()
        )

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.bonjourlemonde:install").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.helloworld:install").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.holamundo:install").getOutcome())
    }

    @Test
    void doesNotFailBuildWhenSnapshotPresentAndReleaseTaskNotStated() {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.bonjourlemonde:install").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.helloworld:install").getOutcome())
        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":service.holamundo:install").getOutcome())
    }
}
