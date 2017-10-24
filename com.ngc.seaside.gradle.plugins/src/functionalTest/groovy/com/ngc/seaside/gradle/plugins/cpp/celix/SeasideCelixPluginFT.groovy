package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.plugins.util.test.TestingUtilities
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
import java.util.jar.Manifest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class SeasideCelixPluginFT {

    private File projectDir
    private Path targetPath
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        targetPath = Paths.get("build/functionalTest/cpp/celix/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = GradleRunner.create().withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        assertEquals(TaskOutcome.valueOf("SUCCESS"),
                            result.task(":service.event.impl.synceventservice:build").getOutcome())

        Path manifestFile = targetPath.resolve(Paths.get(
              "com.ngc.blocs.cpp.service.event.impl.synceventservice",
              "build",
              "distributions",
              "com.ngc.blocs.cpp.service.event.impl.synceventservice-1.0-SNAPSHOT",
              "META-INF"))
        assertTrue("manifest file not created!",
                   manifestFile.toFile().exists())

        manifestFile.toFile().withInputStream { stream ->
            Manifest manifest = new Manifest()
            manifest.read(stream)
            assertEquals("Bundle-SymbolicName not correct",
                         "com.ngc.blocs.cpp.service.event.impl.synceventservice",
                         manifest.getMainAttributes().getValue("Bundle-SymbolicName"))
        }
    }
}
