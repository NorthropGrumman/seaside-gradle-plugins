/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
