package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Test

class SeasideReleaseMonoRepoPluginFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath

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
    void doesUpdateReleaseVersion() {
        SeasideReleaseMonoRepoPlugin plugin = new SeasideReleaseMonoRepoPlugin()
        plugin.apply(project)

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build",
                    SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
                .build()
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
