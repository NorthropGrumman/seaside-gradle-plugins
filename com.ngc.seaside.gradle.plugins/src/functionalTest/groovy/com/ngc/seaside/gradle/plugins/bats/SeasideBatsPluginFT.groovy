package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.plugins.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

class SeasideBatsPluginFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/bats/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.helloworld:build").getOutcome())
    }

    @Test
    void doesRunGradleAnalyzeBuildWithSuccess() {
        makeShellScriptsExecutable()
        BuildResult result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("runBats")
              .build()

        String projectName = "service.holamundo"
        String taskName = "runBats"
        TestingUtilities.AssertTaskSuccess(result, projectName , taskName)
    }

    private void makeShellScriptsExecutable() {
        projectDir.eachFileRecurse { file ->
            if (file.name.endsWith(".sh") || file.name.endsWith(".bash")) {
                def permissions = EnumSet.of(
                     PosixFilePermission.OWNER_READ,
                     PosixFilePermission.OWNER_WRITE,
                     PosixFilePermission.OWNER_EXECUTE,
                     PosixFilePermission.GROUP_READ,
                     PosixFilePermission.GROUP_WRITE,
                     PosixFilePermission.GROUP_EXECUTE,
                     PosixFilePermission.OTHERS_READ,
                     PosixFilePermission.OTHERS_EXECUTE)
                Files.setPosixFilePermissions(file.toPath(), permissions)
            }
         }
    }
}
