package com.ngc.seaside.gradle.tasks.dependencies

import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

@Ignore("This test can take a long time and requires network access.")
class PopulateMaven2RepositoryFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.m2repo").toFile()
        Path targetPath = Paths.get("build/functionalTest/m2repo/com.ngc.example.m2repo")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesPopulateM2Repo() {
        BuildResult result = GradleRunner.create().withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "m2repo", "--stacktrace")
              .build()

        assertEquals("gradle task was not successful",
                     TaskOutcome.valueOf("SUCCESS"),
                     result.task(":m2repo").getOutcome())

        File m2repo = new File(projectDir, "build/m2")
        assertTrue("m2 repo not created!",
                   m2repo.exists())
        assertTrue("m2 repo is empty!",
                   m2repo.listFiles().length > 0)
    }
}
