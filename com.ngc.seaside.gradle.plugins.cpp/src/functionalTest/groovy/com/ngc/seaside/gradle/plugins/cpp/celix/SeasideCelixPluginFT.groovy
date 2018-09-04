/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
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
import java.util.jar.Manifest

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class SeasideCelixPluginFT {

    private File projectDir
    private Path targetPath

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        targetPath = Paths.get("build/functionalTest/cpp/celix/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath()
              .forwardOutput()
              .withArguments(":service.event.impl.synceventservice:clean",
                             ":service.event.impl.synceventservice:build")
              .build()

        assertEquals(TaskOutcome.valueOf("SUCCESS"),
                            result.task(":service.event.impl.synceventservice:build").getOutcome())

        Path manifestFile = targetPath.resolve(Paths.get(
              "com.ngc.blocs.cpp.service.event.impl.synceventservice",
              "build",
              "distributions",
              "com.ngc.blocs.cpp.service.event.impl.synceventservice-1.0-SNAPSHOT",
              "META-INF",
              "MANIFEST.MF"))
        assertTrue("manifest file not created!",
                   manifestFile.toFile().exists())

        manifestFile.toFile().withInputStream { stream ->
            Manifest manifest = new Manifest()
            manifest.read(stream)
            assertEquals("Bundle-SymbolicName not correct!",
                         "com.ngc.blocs.cpp.service.event.impl.synceventservice",
                         manifest.getMainAttributes().getValue("Bundle-SymbolicName"))
            assertEquals("Bundle-Version not correct!",
                         "1.0.0.SNAPSHOT",
                         manifest.getMainAttributes().getValue("Bundle-Version"))
            assertEquals("Bundle-Name not correct!",
                         "com.ngc.blocs.cpp.service.event.impl.synceventservice",
                         manifest.getMainAttributes().getValue("Bundle-Name"))
            assertEquals("Bundle-Activator not correct!",
                         "lib/linux_x86_64/libservice.event.impl.synceventservice.so",
                         manifest.getMainAttributes().getValue("Bundle-Activator"))
            assertEquals("Private-Library not correct!",
                         "lib/linux_x86_64/libservice.event.impl.synceventservice.so",
                         manifest.getMainAttributes().getValue("Private-Library"))
        }
    }
}
