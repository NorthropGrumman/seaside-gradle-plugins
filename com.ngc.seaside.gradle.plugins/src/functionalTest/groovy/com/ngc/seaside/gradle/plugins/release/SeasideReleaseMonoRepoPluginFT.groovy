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
    private List<String> subprojectNames = [
            "service.bonjourlemonde",
            "service.helloworld",
            "service.holamundo",
            "service.konnichiwamojuru"
    ]

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
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
    }

    @Test
    void doesCreateTag() {
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_CREATE_TAG_TASK_NAME)
    }

    @Test
    void doesReleasePush() {
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_PUSH_TASK_NAME)
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

    private void checkForTaskSuccess(String taskName) {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", taskName)
                .build()

        subprojectNames.each { subprojectName ->
            TestingUtilities.assertTaskSuccess(result, subprojectName, taskName)
        }
    }
}
