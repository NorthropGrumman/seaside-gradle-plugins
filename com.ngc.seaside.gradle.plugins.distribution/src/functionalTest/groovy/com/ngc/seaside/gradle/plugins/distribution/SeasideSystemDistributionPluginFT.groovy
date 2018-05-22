package com.ngc.seaside.gradle.plugins.distribution

import static com.ngc.seaside.gradle.plugins.systemdistribution.SeasideSystemDistributionPlugin.*
import static org.junit.Assert.*

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
import java.util.stream.Collectors
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
   }
}
