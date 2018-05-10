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

class SeasideReleasePluginUploadFT {
    private File projectDir

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
    }

    @Test
    void doesUpload() {
        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", "releaseDryRun")
                .build()

        TestingUtilities.assertTaskSuccess(result, "upload-test-project", "releaseDryRun")
    }


    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "upload-test"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "release", "upload-test"
        )
    }
}
