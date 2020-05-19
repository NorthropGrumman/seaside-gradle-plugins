/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
