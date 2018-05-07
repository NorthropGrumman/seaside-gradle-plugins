package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideServiceDistributionPluginFT {

    private File projectDir
    private Project project

    @Before
    void before() {
        File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.distribution").toFile()
        Path targetPath = Paths.get("build/functionalTest/distribution/com.ngc.example.distribution")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }


    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(project.projectDir)
              .withNexusProperties()
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":build").getOutcome())

        Assert.assertTrue("did not create ZIP!",
                          Files.isRegularFile(projectDir.toPath().resolve(Paths.get(
                                "build",
                                "distribution",
                                "com.ngc.seaside.example.distribution-1.0-SNAPSHOT.zip"))))
    }
}
