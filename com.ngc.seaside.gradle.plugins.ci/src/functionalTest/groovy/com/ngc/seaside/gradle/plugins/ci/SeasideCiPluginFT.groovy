package com.ngc.seaside.gradle.plugins.ci

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner

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

class SeasideCiPluginFT {

    private File projectDir
    private Project project

    @Before
    void before() {

        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/parent/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesDisplayGradleProperties() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
              .withNexusProperties()
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("-q", ":nothing", "-Ddisplay.property.name=log4jVersion")
              .build()

        assertEquals("did not print version!",
                     "1.2.17",
                     result.output.split("\n")[0].trim())
    }

    @Test
    void doesUpdateGradleProperties() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
              .withNexusProperties()
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("-q",
                             ":nothing",
                             "-Dupdate.property.name=log4jVersion",
                             "-Dupdate.property.value=9.8.7")
              .build()

        // No (easy) asserts here :(
    }

    @Test
    void doesCreateM2Repo() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
              .withNexusProperties()
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("-q",
                             ":m2repo")
              .build()

        assertEquals("m2repo task was not successful!",
                     TaskOutcome.SUCCESS,
                     result.task(":m2repo").getOutcome())

        File m2Directory = new File(
              projectDir,
              "build/" + SeasideCiPlugin.DEFAULT_M2_OUTPUT_DIRECTORY_NAME)
        assertTrue("m2 directory not created!",
                   m2Directory.isDirectory())
        assertTrue("m2 directory not populated!",
                   m2Directory.listFiles().length > 0)

        File m2Archive = new File(
              projectDir,
              "build/" + SeasideCiExtension.DEFAULT_M2_ARCHIVE_NAME)
        assertTrue("m2 archive not created!",
                   m2Archive.isFile())

        File dependencyReport = new File(
              projectDir,
              "build/" + SeasideCiExtension.DEFAULT_M2_ARCHIVE_NAME)
        assertTrue("dependency report not created!",
                   dependencyReport.isFile())

        File deploymentScript = new File(
              projectDir,
              "build/" + SeasideCiPlugin.DEFAULT_M2_DEPLOYMENT_SCRIPT_NAME)
        assertTrue("deployment script not created!",
                   deploymentScript.isFile())

        File settingsFile = new File(
              projectDir,
              "build/settings.xml")
        assertTrue("settings file not created!",
                   settingsFile.isFile())
    }
}
