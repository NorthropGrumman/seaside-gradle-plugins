package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideBatsPluginIT {
   private SeasideBatsExtension extension
   private SeasideBatsPlugin plugin
   private Project project
   private File projectDir

   @Before
   void before() {
      File source = Paths.get("src/test/resources/bats-plugin/sealion-java-hello-world").toFile()
      projectDir = Files.createDirectories(Paths.get("build/test-bats-plugin/sealion-java-hello-world")).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()
      extension = new SeasideBatsExtension(project)
      plugin = new SeasideBatsPlugin()
      plugin.apply(project)
   }

   @Test
   void appliesPlugin() {
      Assert.assertNotNull(project.extensions.findByName(SeasideBatsPlugin.BATS_EXTENSION_NAME))
      Assert.assertNotNull(project.tasks.findByName(SeasideBatsPlugin.EXTRACT_BATS_TASK_NAME))
      Assert.assertNotNull(project.tasks.findByName(SeasideBatsPlugin.RUN_BATS_TASK_NAME))
   }

   @Test
   void batsIsExtractedToTheCorrectDirectory() {
      println Paths.get(extension.BATS_PATHS.PATH_TO_THE_DIRECTORY_WITH_BATS_SCRIPTS).toAbsolutePath()
      Assert.assertNotNull(Paths.get(extension.BATS_PATHS.PATH_TO_THE_DIRECTORY_WITH_BATS_SCRIPTS).toAbsolutePath().toFile())
   }
}
