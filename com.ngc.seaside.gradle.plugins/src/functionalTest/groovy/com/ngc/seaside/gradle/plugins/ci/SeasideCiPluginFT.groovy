package com.ngc.seaside.gradle.plugins.ci

import com.ngc.seaside.gradle.plugins.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.*

class SeasideCiPluginFT {

    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/parent/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesDisplayGradleProperties() {
        BuildResult result = GradleRunner.create().withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("-q", ":service.konnichiwamojuru:nothing", "-Ddisplay.property.name=log4jVersion")
              .build()

        assertEquals("did not print version!",
                     "1.2.17",
                     result.output.split("\n")[0].trim())
    }

    @Test
    void doesUpdateGradleProperties() {
        BuildResult result = GradleRunner.create().withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("-q",
                             ":service.konnichiwamojuru:nothing",
                             "-Dupdate.property.name=log4jVersion",
                             "-Dupdate.property.value=9.8.7")
              .build()

        // No (easy) asserts here :(
    }
}
