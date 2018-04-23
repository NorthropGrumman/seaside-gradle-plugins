package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

class SeasideEclipseUpdateSitePluginFT {
    private List<File> pluginClasspath
    private Project project
    private File projectDir

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
              sourceDirectoryWithTheTestProject(),
              pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Test
    void runsGradleBuildWithSuccess() {
        BuildResult result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        TestingUtilities.assertTaskSuccess(result, "service.heiverden", "build")
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
              "src", "functionalTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
              "build", "functionalTest", "eclipse", "updatesite", "sealion-java-hello-world"
        )
    }
}
