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
package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class SeasideReleasePluginFT {
    private File projectDir
    private Project project

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Test
    void doesReleaseWhenSnapshotIsSpecified() {
        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "releaseDryRun")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "releaseDryRun")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "releaseDryRun")
    }

    @Test
    void doesFailWhenReleasingAndSnapshotIsNotSpecified() {
        FileUtils.copyFile(
            Paths.get(sourceDirectoryWithTheTestProject().toString(), "build-without-snapshot.gradle").toFile(),
            Paths.get(pathToTheDestinationProjectDirectory().toString(), "build.gradle").toFile()
        )

        SeasideReleasePlugin plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .buildAndFail()

        Assert.assertNull(result.task(":clean"))
        Assert.assertNull(result.task(":build"))
        Assert.assertNull(result.task(":releaseDryRun"))
    }

    @Test
    void doesNotFailBuildWhenSnapshotMissingAndReleaseTaskNotStated() {
        FileUtils.copyFile(
            Paths.get(sourceDirectoryWithTheTestProject().toString(), "build-without-snapshot.gradle").toFile(),
            Paths.get(pathToTheDestinationProjectDirectory().toString(), "build.gradle").toFile()
        )

        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "install")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "install")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "install")
    }

    @Test
    void doesNotFailBuildWhenSnapshotPresentAndReleaseTaskNotStated() {
        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", "install")
                .build()

        TestingUtilities.assertTaskSuccess(result, "service.bonjourlemonde", "install")
        TestingUtilities.assertTaskSuccess(result, "service.helloworld", "install")
        TestingUtilities.assertTaskSuccess(result, "service.holamundo", "install")
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "release", "sealion-java-hello-world"
        )
    }
}
