package com.ngc.seaside.gradle.plugins.application

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

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assume.assumeTrue

class SeasideApplicationPluginFT {

   private File projectDir
   private Path targetPath
   private Project project

   @Before
   void before() {
      File source = Paths.get("src/functionalTest/resources/application").toFile()
      targetPath = Paths.get("build/functionalTest/application")
      projectDir = Files.createDirectories(targetPath).toFile()
      FileUtils.copyDirectory(source, projectDir)

      project = ProjectBuilder.builder().withProjectDir(projectDir).build()
   }

   @Test
   void doesRunGradleBuildWithSuccess() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build")
            .build()

      assertEquals("outcome of task not correct!",
                   TaskOutcome.valueOf("SUCCESS"),
                   result.task(":build").getOutcome())
      verifyBashScript()
      verifyBatScript()
   }

   private void verifyBashScript() {
      Path linuxStartScript = targetPath.resolve(Paths.get("build",
                                                           "distributions",
                                                           "application-ft-1.0.0-SNAPSHOT",
                                                           "bin",
                                                           "application-ft"))
      assertTrue("${linuxStartScript} does not exist!", Files.isRegularFile(linuxStartScript))

      List<String> contents = Files.readAllLines(linuxStartScript)
      String properties = contents.stream()
            .filter({ l -> l.startsWith("DEFAULT_JVM_OPTS") })
            .findFirst()
            .orElseThrow({ new AssertionError("script does not contain JVM system properties!") })
      assertEquals("properties not correct!",
                   "DEFAULT_JVM_OPTS=' \"-DMY_PROP=\$APP_HOME\"  \"-DappHome=\$APP_HOME\" '",
                   properties)

      assumeTrue("only check file permissions on Linux", OperatingSystem.current().isLinux());
      assertEquals(
            "linux start script has incorrect permissions",
            "rwxr-xr-x",
            PosixFilePermissions.toString(Files.getPosixFilePermissions(linuxStartScript)))
   }

   private void verifyBatScript() {
      List<String> contents = Files.readAllLines(targetPath.resolve(Paths.get("build",
                                                                              "distributions",
                                                                              "application-ft-1.0.0-SNAPSHOT",
                                                                              "bin",
                                                                              "application-ft.bat")))
      String properties = contents.stream()
            .filter({ l -> l.startsWith("set DEFAULT_JVM_OPTS") })
            .findFirst()
            .orElseThrow({ new AssertionError("script does not contain JVM system properties!") })
      assertEquals("properties not correct!",
                   "set DEFAULT_JVM_OPTS= \"-DMY_PROP=%APP_HOME%\"  \"-DappHome=%APP_HOME%\" ",
                   properties)
   }
}
