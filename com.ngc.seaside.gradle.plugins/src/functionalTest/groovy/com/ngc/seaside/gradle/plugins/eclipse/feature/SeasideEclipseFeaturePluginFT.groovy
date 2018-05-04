package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideEclipseFeaturePluginFT {
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

        TestingUtilities.assertTaskSuccess(result, "service.nihao", "build")

        Assert.assertTrue(
              "feature file not copied!",
              project.file(
                    TestingUtilities.turnListIntoPath(
                          projectDir.absolutePath, "com.ngc.seaside.service.nihao", "build", "tmp", "feature.xml"
                    )).exists())
        Assert.assertTrue(
              "jar file file was not created!",
              project.file(
                    TestingUtilities.turnListIntoPath(
                          projectDir.absolutePath, "com.ngc.seaside.service.nihao", "build",
                          "com.ngc.seaside.service.nihao-1.2.3-SNAPSHOT.jar"
                    )).exists())
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
              "src", "functionalTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
              "build", "functionalTest", "eclipse", "feature", "sealion-java-hello-world"
        )
    }
}
