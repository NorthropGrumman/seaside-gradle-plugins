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
