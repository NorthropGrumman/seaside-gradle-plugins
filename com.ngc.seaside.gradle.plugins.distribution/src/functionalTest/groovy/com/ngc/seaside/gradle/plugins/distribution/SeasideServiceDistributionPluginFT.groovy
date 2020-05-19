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
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

class SeasideServiceDistributionPluginFT {

   private File projectDir
   private Project project

   @Before
   void before() {
      File source = Paths.get('src/functionalTest/resources/distribution/com.ngc.example.distribution').toFile()
      Path targetPath = Paths.get('build/functionalTest/distribution/com.ngc.example.distribution')
      projectDir = Files.createDirectories(targetPath).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()
   }


   @Test
   void doesRunGradleBuildWithSuccess() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(project.projectDir)
            .withNexusProperties()
            .withPluginClasspath()
            .forwardOutput()
            .withArguments('clean', 'build')
            .build()

      Assert.assertEquals(TaskOutcome.valueOf('SUCCESS'), result.task(':build').getOutcome())

      Path distDir = projectDir.toPath().resolve(Paths.get('build', 'distribution'))
      Path zipFile = distDir.resolve('com.ngc.seaside.example.distribution-1.0-SNAPSHOT.zip')
      Path unzippedDir = distDir.resolve('com.ngc.seaside.example.distribution-1.0-SNAPSHOT')
      Path binDir = unzippedDir.resolve('bin')
      Path linuxStartScript = binDir.resolve('start')

      Assert.assertTrue('did not create ZIP!', Files.isRegularFile(zipFile))
      Assert.assertTrue("${unzippedDir} does not exist", Files.isDirectory(unzippedDir))
      Assert.assertTrue("${binDir}/bin does not exist", Files.isDirectory(binDir))
      Assert.assertTrue("${linuxStartScript} does not exist", Files.isRegularFile(linuxStartScript))
      Assume.assumeTrue("only check file permissions on Linux", OperatingSystem.current().isLinux());
      Assert.assertEquals(
            'linux start script has incorrect permissions',
            'rwxr-xr-x',
            PosixFilePermissions.toString(Files.getPosixFilePermissions(linuxStartScript)))
   }
}
