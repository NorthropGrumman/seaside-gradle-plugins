package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

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
        BuildResult result = SeasideGradleRunner.create()
              .withNexusProperties()
              .withProjectDir(projectDir)
              .withPluginClasspath(pluginClasspath)
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        TestingUtilities.assertTaskSuccess(result, "service.heiverden", "build")

        String updateSitePath = Paths.get(
              pathToTheDestinationProjectDirectory().absolutePath,
              "com.ngc.seaside.service.heiverden", "build", "updatesite")
        String featuresPath = Paths.get(updateSitePath, "features")
        String pluginsPath = Paths.get(updateSitePath, "plugins")
        String updateSiteArchivePath = Paths.get(updateSitePath, "com.ngc.seaside.test.test-name-1.0.0.zip")

        Assert.assertTrue("$featuresPath directory was not created!", project.file(featuresPath).exists())
        Assert.assertTrue("$pluginsPath directory was not created!", project.file(pluginsPath).exists())
        Assert.assertTrue(
              "$updateSiteArchivePath directory was not created!", project.file(updateSiteArchivePath).exists())
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
