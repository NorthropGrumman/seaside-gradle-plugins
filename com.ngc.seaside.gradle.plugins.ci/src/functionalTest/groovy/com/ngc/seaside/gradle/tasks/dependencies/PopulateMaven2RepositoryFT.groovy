/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.tasks.dependencies

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
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

   @Before
   void before() {
      File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.m2repo").toFile()
      Path targetPath = Paths.get("build/functionalTest/m2repo/com.ngc.example.m2repo")
      projectDir = Files.createDirectories(targetPath).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()
   }

   @Test
   void doesPopulateM2RepoAndCreateReportAndScript() {
      BuildResult result = SeasideGradleRunner.create()
            .withProjectDir(projectDir)
            .withNexusProperties()
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("populateM2repo", "--stacktrace")
            .build()

      assertEquals("gradle task was not successful",
                   TaskOutcome.valueOf("SUCCESS"),
                   result.task(":populateM2repo").getOutcome())

      File m2repo = new File(projectDir, "build/dependencies-m2")
      assertTrue("m2 repo not created!",
                 m2repo.exists())
      assertTrue("m2 repo is empty!",
                 m2repo.listFiles().length > 0)

      File dependencyReport = new File(
            projectDir,
            "build/dependencies.tsv")
      assertTrue("dependency report not created!",
                 dependencyReport.isFile())

      File script = new File(
            projectDir,
            "build/deploy.sh")
      assertTrue("script not created!",
                 script.isFile())

      File settings = new File(
            projectDir,
            "build/settings.xml")
      assertTrue("settings not created!",
                 settings.isFile())
   }
}
