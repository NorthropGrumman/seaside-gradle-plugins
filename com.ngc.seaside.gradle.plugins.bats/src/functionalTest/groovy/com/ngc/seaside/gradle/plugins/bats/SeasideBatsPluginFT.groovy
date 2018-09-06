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
package com.ngc.seaside.gradle.plugins.bats

import static org.junit.Assume.assumeFalse

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

class SeasideBatsPluginFT {
    private File projectDir
    private Project project

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
              sourceDirectoryWithTheTestProject(),
              pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Ignore("TODO: Fix this plugin")
    @Test
    void doesRunBatsTestsWithSuccess() {
        makeShellScriptsExecutable()
        BuildResult result = SeasideGradleRunner.create()
              .withNexusProperties()
              .withProjectDir(projectDir)
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("runBats")
              .build()

        TestingUtilities.assertTaskSuccess(result, "service.holamundo" , "runBats")
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
              "src", "functionalTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
              "build", "functionalTest",
              "bats", "sealion-java-hello-world"
        )
    }

    private void makeShellScriptsExecutable() {
        projectDir.eachFileRecurse { file ->
            if (file.name.endsWith(".sh") || file.name.endsWith(".bash")) {
                def permissions = EnumSet.of(
                     PosixFilePermission.OWNER_READ,
                     PosixFilePermission.OWNER_WRITE,
                     PosixFilePermission.OWNER_EXECUTE,
                     PosixFilePermission.GROUP_READ,
                     PosixFilePermission.GROUP_WRITE,
                     PosixFilePermission.GROUP_EXECUTE,
                     PosixFilePermission.OTHERS_READ,
                     PosixFilePermission.OTHERS_EXECUTE)
                Files.setPosixFilePermissions(file.toPath(), permissions)
            }
         }
    }
}
