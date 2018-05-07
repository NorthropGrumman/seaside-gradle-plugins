package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

import static org.junit.Assert.assertTrue

class SeasideEclipseUpdateSitePluginFT {
    private Project project
    private File projectDir

    @Before
    void before() {
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
              .withPluginClasspath()
              .forwardOutput()
              .withArguments("clean", "build")
              .build()

        TestingUtilities.assertTaskSuccess(result, "service.heiverden", "build")

        String projectPrefix = "com.ngc.seaside.service"
        String projectBuildDir = Paths.get(projectDir.absolutePath, "${projectPrefix}.heiverden", "build")
        String updateSitePath = Paths.get(projectBuildDir, "updatesite")
        String featuresPath = Paths.get(updateSitePath, "features", "${projectPrefix}.nihao_1.2.3.SNAPSHOT.jar")
        String customPluginPath = Paths.get(updateSitePath, "plugins", "${projectPrefix}.helloworld_1.2.3.SNAPSHOT.jar")
        String eclipsePluginPath = Paths.get(updateSitePath, "plugins", "org.antlr.runtime_3.2.0.v201101311130.jar")
        String artifactsPath = Paths.get(updateSitePath, "artifacts.jar")
        String contentPath = Paths.get(updateSitePath, "content.jar")
        String updateSiteArchivePath = Paths.get(projectBuildDir, "com.ngc.seaside.test.test-name-1.0.0.zip")

        assertTrue("$featuresPath directory was not created!", project.file(featuresPath).exists())
        assertTrue("$customPluginPath directory was not created!", project.file(customPluginPath).exists())
        assertTrue("$eclipsePluginPath directory was not created!", project.file(eclipsePluginPath).exists())
        assertTrue("$artifactsPath directory was not created!", project.file(artifactsPath).exists())
        assertTrue("$contentPath directory was not created!", project.file(contentPath).exists())
        assertTrue("$updateSiteArchivePath directory was not created!", project.file(updateSiteArchivePath).exists())
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
