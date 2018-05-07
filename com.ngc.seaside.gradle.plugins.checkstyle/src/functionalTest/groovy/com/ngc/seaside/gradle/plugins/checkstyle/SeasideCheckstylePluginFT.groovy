package com.ngc.seaside.gradle.plugins.checkstyle

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideCheckstylePluginFT {

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
   void doesRunGradleCleanBuildCheckstyleMainWithSuccess() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
            .withNexusProperties()
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build", "checkstyleMain")
            .build()
      Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":checkstyleMain").getOutcome())
   }
}
