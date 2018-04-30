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

        String projectName = "com.ngc.seaside.service.heiverden"
        String projectBuildDir = Paths.get(pathToTheDestinationProjectDirectory().absolutePath, projectName, "build")
        String updateSitePath = Paths.get(projectBuildDir, "updatesite")
        String featuresPath = Paths.get(updateSitePath, "features", "com.ngc.seaside.service.nihao_1.2.3.SNAPSHOT.jar")
        String customPluginPath = Paths.get(
              updateSitePath, "plugins", "com.ngc.seaside.service.helloworld_1.2.3.SNAPSHOT.jar")
        String eclipsePluginPath = Paths.get(updateSitePath, "plugins", "org.antlr.runtime_3.2.0.v201101311130.jar")
        String updateSiteArchivePath = Paths.get(projectBuildDir, "com.ngc.seaside.test.test-name-1.0.0.zip")

        Assert.assertTrue("$featuresPath directory was not created!", project.file(featuresPath).exists())
        Assert.assertTrue("$customPluginPath directory was not created!", project.file(customPluginPath).exists())
        Assert.assertTrue("$eclipsePluginPath directory was not created!", project.file(eclipsePluginPath).exists())
        Assert.assertTrue(
              "artifacts.jar directory was not created!",
              project.file(Paths.get(updateSitePath, "artifacts.jar")).exists())
        Assert.assertTrue(
              "content.jar directory was not created!",
              project.file(Paths.get(updateSitePath, "content.jar")).exists())
        Assert.assertTrue(
              "$updateSiteArchivePath directory was not created!",
              project.file(updateSiteArchivePath).exists())
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
