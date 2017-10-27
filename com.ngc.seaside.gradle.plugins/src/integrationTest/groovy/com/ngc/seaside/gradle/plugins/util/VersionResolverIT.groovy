package com.ngc.seaside.gradle.plugins.util

import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class VersionResolverIT {
   private File projectDir
   private Project project
   private SeasideReleasePlugin plugin
   private VersionResolver resolver

   @Before
   void before() {
      File source = Paths.get("src/integrationTest/resources/sealion-java-hello-world").toFile()
      Path targetPath = Paths.get("build/integrationTest/resources/util/sealion-java-hello-world")
      projectDir = Files.createDirectories(targetPath).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()

      resolver = new VersionResolver(project)

      plugin = new SeasideReleasePlugin()
      plugin.apply(project)
   }

   @Test
   void doesSetProjectVersionOnFile() {
      def version = "4.5.6"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals(version, resolver.getProjectVersion())

      version = "4.5.6-SUFFIX"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals(version, resolver.getProjectVersion())
   }
}
