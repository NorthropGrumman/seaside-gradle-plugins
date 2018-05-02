package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import static org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import static com.ngc.seaside.gradle.plugins.distribution.SeasideFelixServiceDistributionPlugin.*
class SeasideFelixServiceDistributionPluginFT {

   private Path projectDir
   private Project project
   private List<File> pluginClasspath

   @Before
   void before() {
      pluginClasspath = TestingUtilities.getTestClassPath(getClass())

      File source = Paths.get("src/functionalTest/resources/distribution/com.ngc.example.felixdistribution").toFile()
      projectDir = Paths.get("build/functionalTest/distribution/com.ngc.example.felixdistribution")
      Files.createDirectories(projectDir)
      FileUtils.copyDirectory(source, projectDir.toFile())
      project = ProjectBuilder.builder().withProjectDir(projectDir.toFile()).build()
   }


   @Test
   void doesRunGradleBuildWithSuccess() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(project.projectDir)
            .withNexusProperties()
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withArguments("clean", "build", "-S")
            .build()

      assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":build").getOutcome())

      Path distDir = projectDir.resolve(Paths.get("build", DISTRIBUTION_DIRECTORY));
      Path zipFile = distDir.resolve("com.ngc.seaside.example.felix.distribution-1.0-SNAPSHOT.zip")
      assertTrue("did not create ZIP!", Files.isRegularFile(zipFile))
      Path unzippedDir = distDir.resolve('com.ngc.seaside.example.felix.distribution-1.0-SNAPSHOT');
      assertTrue(Files.isDirectory(unzippedDir))
      assertTrue(Files.isDirectory(unzippedDir.resolve(BIN_DIRECTORY)))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(BIN_DIRECTORY).resolve('start.bat')))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(BIN_DIRECTORY).resolve('start.sh')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(RESOURCES_DIRECTORY)))
      assertTrue(Files.isRegularFile(unzippedDir.resolve(RESOURCES_DIRECTORY).resolve('example.scenario')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(PLATFORM_DIRECTORY)))
      assertTrue(Files.list(unzippedDir.resolve(PLATFORM_DIRECTORY)).count() >= DEFAULT_PLATFORM_DEPENDENCIES.size());
      assertTrue(Files.isRegularFile(unzippedDir.resolve(CONFIG_DIRECTORY).resolve('config.properties')))
      assertTrue(Files.isDirectory(unzippedDir.resolve(BUNDLES_DIRECTORY)))
      assertTrue(Files.list(unzippedDir.resolve(BUNDLES_DIRECTORY)).count() >= DEFAULT_BUNDLE_DEPENDENCIES.size());
      Set<String> bundles = Files.list(unzippedDir.resolve(BUNDLES_DIRECTORY))
            .collect(Collectors.toSet())
            .collect { it.fileName}
            .collect { it.toString() }
      assertTrue(bundles.toString(), 'org.eclipse.xtend.org.eclipse.xtend.lib-2.13.0.jar' in bundles)
      assertTrue(bundles.toString(), 'org.eclipse.xtend.org.eclipse.xtend.lib-2.12.0.jar' in bundles)
      assertTrue(bundles.toString(), 'org.eclipse.xtend.org.eclipse.xtend.lib.macro-2.13.0.jar' in bundles)
      assertFalse(bundles.toString(), 'org.eclipse.xtend.org.eclipse.xtend.lib.macro-2.11.0.jar' in bundles)
      assertFalse(bundles.toString(), 'org.eclipse.xtend.org.eclipse.xtend.lib.macro-2.12.0.jar' in bundles)
      assertFalse(bundles.toString(), 'commons-collections.commons-collections-3.0.jar' in bundles)
      assertFalse(bundles.toString(), 'org.osgi.org.osgi.core-5.0.jar' in bundles)
      assertFalse(bundles.toString(), 'org.osgi.org.osgi.core-6.0.jar' in bundles)
   }
}
