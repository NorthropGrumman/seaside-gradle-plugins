package com.ngc.seaside.gradle.plugins.command

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class SeasideCommandPluginFT {

    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/command/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
              .withNexusProperties()
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.konnichiwamojuru:build").getOutcome())

        assertTrue("command parent plugin failed to create template zip for project with multiple templates!",
                   projectDir.toPath().resolve(Paths.get(
                         "com.ngc.seaside.service.konnichiwamojuru",
                         "build",
                         "libs",
                         "com.ngc.seaside.service.konnichiwamojuru-1.2.3-SNAPSHOT-template-bar.zip"))
                         .toFile()
                         .isFile())
        assertTrue("command parent plugin failed to create template zip for project with multiple templates!",
                   projectDir.toPath().resolve(Paths.get(
                         "com.ngc.seaside.service.konnichiwamojuru",
                         "build",
                         "libs",
                         "com.ngc.seaside.service.konnichiwamojuru-1.2.3-SNAPSHOT-template-foo.zip"))
                         .toFile()
                         .isFile())
    }
}
