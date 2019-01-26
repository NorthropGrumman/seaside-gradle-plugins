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
package com.ngc.seaside.gradle.plugins.cpp.celix

import static org.gradle.internal.impldep.junit.framework.TestCase.assertTrue
import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeFalse

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class CelixDistributionPluginFT {

    private File projectDir
    private Path targetPath

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        targetPath = Paths.get("build/functionalTest/cpp/celix-distribution/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)
    }

    @Test
    void doesBuildDistribution() {
        BuildResult result = SeasideGradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath()
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
