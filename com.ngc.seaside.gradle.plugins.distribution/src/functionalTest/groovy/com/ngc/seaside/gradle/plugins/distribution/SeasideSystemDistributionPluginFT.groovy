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
package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

import static com.ngc.seaside.gradle.plugins.systemdistribution.SeasideSystemDistributionPlugin.DISTRIBUTION_DIRECTORY
import static org.junit.Assert.*
import static org.junit.Assume.assumeTrue

class SeasideSystemDistributionPluginFT {

   private Path projectDir
   private Project project

   @Before
   void before() {
      File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.systemdistribution").toFile()
      projectDir = Paths.get("build/functionalTest/distribution/com.ngc.example.systemdistribution")
      Files.createDirectories(projectDir)
      FileUtils.copyDirectory(source, projectDir.toFile())
      project = ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build()
   }


   @Test
   void doesRunGradleBuildWithSuccess() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(project.projectDir)
            .withNexusProperties()
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build", "publishToMavenLocal", "-S")
            .build()

      assertEquals(TaskOutcome.SUCCESS, result.task(":build").getOutcome())
      assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal").getOutcome())
      result.tasks.each {
         assertNotEquals(TaskOutcome.FAILED, it.outcome)
      }

      Path distDir = projectDir.resolve(Paths.get("build", DISTRIBUTION_DIRECTORY));
      Path zipFile = distDir.resolve("com.ngc.seaside.example.system.distribution-1.0-SNAPSHOT.zip")
      assertTrue("did not create ZIP!", Files.isRegularFile(zipFile))
      Path unzippedDir = distDir.resolve('com.ngc.seaside.example.system.distribution-1.0-SNAPSHOT')
      assertTrue(Files.isDirectory(unzippedDir))
      assertTrue(Files.isDirectory(unzippedDir.resolve('com.ngc.seaside.threateval.ctps.distribution-2.4.0')))
      assertTrue(Files.isDirectory(unzippedDir.resolve('com.ngc.seaside.threateval.etps.distribution-2.4.0')))
      assertTrue(Files.isDirectory(unzippedDir.resolve('com.ngc.seaside.threateval.datps.distribution-2.4.0')))
      assertTrue(Files.isDirectory(unzippedDir.resolve('com.ngc.seaside.threateval.tps.distribution-2.4.0')))
      assertTrue(Files.isRegularFile(unzippedDir.resolve('start.bat')))
      assertTrue(Files.isRegularFile(unzippedDir.resolve('start.sh')))
      assertTrue(Files.isRegularFile(unzippedDir.resolve('example.resource')))

      assumeTrue("only check file permissions on Linux", OperatingSystem.current().isLinux());
      assertEquals(
            "linux start script has incorrect permissions",
            "rwxr-xr-x",
            PosixFilePermissions.toString(Files.getPosixFilePermissions(unzippedDir.resolve('start.sh'))))
   }
}
