/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.command

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

class SeasideCommandPluginFT {

    private File projectDir
    private Project project

    @Before
    void before() {
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
              .withPluginClasspath()
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
