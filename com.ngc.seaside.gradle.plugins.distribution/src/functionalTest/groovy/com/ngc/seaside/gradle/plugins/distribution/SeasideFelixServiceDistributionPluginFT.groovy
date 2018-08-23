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
import java.util.stream.Collectors

import static com.ngc.seaside.gradle.plugins.distribution.SeasideFelixServiceDistributionPlugin.*
import static org.junit.Assert.*
import static org.junit.Assume.assumeTrue

class SeasideFelixServiceDistributionPluginFT {

   private Path projectDir
   private Project project

   @Before
   void before() {
      File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.felixdistribution").toFile()
      projectDir = Paths.get("build/functionalTest/distribution/com.ngc.example.felixdistribution")
      Files.createDirectories(projectDir)
      FileUtils.copyDirectory(source, projectDir.toFile())
      project = ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build()
   }

   @Test
   void doesRunGradleBuildWithSuccess() {
      BuildResult result = SeasideGradleRunner.create()
            .withProjectDir(project.projectDir)
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
      Path distDir = projectDir.resolve(Paths.get("build", DISTRIBUTION_DIRECTORY))
      Path zipFile = distDir.resolve("com.ngc.seaside.example.felix.distribution-1.0-SNAPSHOT.zip")
      assertTrue("did not create ZIP!", Files.isRegularFile(zipFile))
      Path unzippedDir = distDir.resolve('com.ngc.seaside.example.felix.distribution-1.0-SNAPSHOT')
      assertTrue(Files.isDirectory(unzippedDir))
      assertTrue(Files.isDirectory(unzippedDir.resolve(BIN_DIRECTORY)))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(BIN_DIRECTORY).resolve('start.bat')))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(BIN_DIRECTORY).resolve('start.sh')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(RESOURCES_DIRECTORY)))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(RESOURCES_DIRECTORY).resolve('example.scenario')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(PLATFORM_DIRECTORY)))
      assertTrue(Files.list(unzippedDir.resolve(PLATFORM_DIRECTORY)).count() >= DEFAULT_PLATFORM_DEPENDENCIES.size())
      assertTrue(Files.isRegularFile(unzippedDir.resolve(CONFIG_DIRECTORY).resolve('config.properties')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(BUNDLES_DIRECTORY)))
      assertTrue(Files.list(unzippedDir.resolve(BUNDLES_DIRECTORY)).count() >= DEFAULT_BUNDLE_DEPENDENCIES.size())
      Set<String> bundles = Files.list(unzippedDir.resolve(BUNDLES_DIRECTORY))
            .collect(Collectors.toSet())
            .collect { it.fileName }
            .collect { it.toString() }
      def jars = ['org.eclipse.xtend.lib_2.13.0', 'org.eclipse.xtend.lib_2.12.0', 'org.eclipse.xtend.lib.macro_2.13.0', 'org.eclipse.xtend.lib.macro_2.11.0']
      for (def jar : jars) {
         assertTrue("Expected ${jar} to be included in the bundles", bundles.any { it.contains jar })
      }
      def nonjars = ['org.eclipse.xtend.lib.macro_2.12.0', 'commons-collections_3.0']
      for (def jar : nonjars) {
         assertFalse("Expected ${jar} not to be included in the bundles", bundles.any { it.contains jar })
      }

      assumeTrue("only check file permissions on Linux", OperatingSystem.current().isLinux());
      def linuxStartScript = unzippedDir.resolve(BIN_DIRECTORY).resolve('start.sh')
      assertEquals(
            "linux start script has incorrect permissions",
            "rwxr-xr-x",
            PosixFilePermissions.toString(Files.getPosixFilePermissions(linuxStartScript)))
   }
}
