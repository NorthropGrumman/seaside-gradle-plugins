package com.ngc.seaside.gradle.plugins.ci

import com.ngc.seaside.gradle.extensions.ci.SeasideCiExtension
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

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

    @Test
    void doesCreateM2Repo() {
        BuildResult result = GradleRunner.create().withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("-q",
                             ":service.konnichiwamojuru:clean",
                             ":service.konnichiwamojuru:m2repo")
              .build()

        assertEquals("m2repo task was not successful!",
                     TaskOutcome.SUCCESS,
                     result.task(":service.konnichiwamojuru:m2repo").getOutcome())

        File m2Directory = new File(
              projectDir,
              "com.ngc.seaside.service.konnichiwamojuru/build/" + SeasideCiPlugin.DEFAULT_M2_OUTPUT_DIRECTORY_NAME)
        assertTrue("m2 directory not created!",
                   m2Directory.isDirectory())
        assertTrue("m2 directory not populated!",
                   m2Directory.listFiles().length > 0)

        File m2Archive = new File(
              projectDir,
              "com.ngc.seaside.service.konnichiwamojuru/build/" + SeasideCiExtension.DEFAULT_M2_ARCHIVE_NAME)
        assertTrue("m2 archive not created!",
                   m2Archive.isFile())

        File csvReport = new File(
              projectDir,
              "com.ngc.seaside.service.konnichiwamojuru/build/" +
              SeasideCiPlugin.DEFAULT_CSV_FILE_NAME)
        assertTrue("csv report not created!",
                   csvReport.isFile())
    }
}
