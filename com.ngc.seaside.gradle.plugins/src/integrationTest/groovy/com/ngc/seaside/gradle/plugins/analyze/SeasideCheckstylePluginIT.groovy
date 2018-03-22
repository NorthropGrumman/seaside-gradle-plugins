package com.ngc.seaside.gradle.plugins.analyze

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideCheckstylePluginIT {

   private File projectDir
   private Project project
   private SeasideCheckstylePlugin plugin

   @Before
   void before() {
      File source = Paths.get("src/integrationTest/resources/sealion-java-hello-world").toFile()
      Path targetPath = Paths.get("build/integrationTest/resources/parent/com.ngc.example.parent")
      projectDir = Files.createDirectories(targetPath).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()

      plugin = new SeasideCheckstylePlugin()

      setRequiredProjectProperties(project)
   }

   @Test
   void doesApplyPlugin() {
      plugin.apply(project)

      TaskResolver resolver = new TaskResolver(project)
      Assert.assertNotNull(resolver.findTask("checkstyleMain"))
   }

   static void setRequiredProjectProperties(Project project) {
      String test = "test"
      project.ext.nexusReleases = test
      project.ext.nexusUsername = test
      project.ext.nexusPassword = test
      project.ext.nexusSnapshots = test
      project.ext.nexusConsolidated = test
   }
}
