package com.ngc.seaside.gradle.plugins.distribution

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

class SeasideDistributionPluginFT {


    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        URL pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }
        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }

        File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.distribution").toFile()
        Path targetPath = Paths.get("build/functionalTest/resources/distribution/com.ngc.example.distribution")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }


    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = GradleRunner.create().withProjectDir(project.projectDir)
                .withPluginClasspath(pluginClasspath)
                .withArguments("clean", "build")
                .build()

        Assert.assertEquals(TaskOutcome.valueOf( "SUCCESS"), result.task(":build").getOutcome())
    }
}
