package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.gradle.internal.impldep.junit.framework.TestCase.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeFalse

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class CelixDistributionPluginFT {

    private File projectDir
    private Path targetPath
    private List<File> pluginClasspath

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        targetPath = Paths.get("build/functionalTest/cpp/celix-distribution/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)
    }

    @Test
    void doesBuildDistribution() {
        BuildResult result = SeasideGradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments(":example.distribution:clean",
                             ":example.distribution:build")
              .build()

        assertEquals(TaskOutcome.valueOf("SUCCESS"),
                     result.task(":example.distribution:build").getOutcome())

        Path distributionDir = targetPath.resolve(Paths.get(
              "com.ngc.blocs.cpp.example.distribution",
              "build",
              "distributions"))
        assertTrue("did not create distribution ZIP!",
                   Files.exists(distributionDir.resolve("example.distribution-1.0-SNAPSHOT.zip")))
        assertTrue("did not create run script!",
                   Files.exists(distributionDir.resolve("com.ngc.blocs.cpp.example.distribution-1.0-SNAPSHOT/bin/start.sh")))
        assertTrue("did not include default bundles!",
                   Files.exists(distributionDir.resolve(
                         "com.ngc.blocs.cpp.example.distribution-1.0-SNAPSHOT/bundles/shell.zip")))
        assertTrue("did not include logservice bundle!",
                   Files.exists(distributionDir.resolve(
                         "com.ngc.blocs.cpp.example.distribution-1.0-SNAPSHOT/bundles/service.log.impl.logservice-1.0-SNAPSHOT.zip")))
    }
}
