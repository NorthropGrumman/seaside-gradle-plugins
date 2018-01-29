package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Assert
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
        setupTestingGitRepo()
    }

    @After
    void after() {
        if (projectDir != null) {
            projectDir.deleteDir()
        }
    }

    @Test
    void doesUpdateReleaseVersion() {
        // TODO(Cameron): fix this test so that it runs correctly
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--pretty=format:%s")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertTrue(
                  "output did not contain expected release message!\noutput:" + output.toString(),
                  output.toString().split("\n")[0].startsWith("Release of version v")
            )
        }
    }

    @Test
    void doesCreateTag() {
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_CREATE_TAG_TASK_NAME)
    }

    @Test
    void doesBumpVersion() {
        checkForTaskSuccess(SeasideReleaseMonoRepoPlugin.RELEASE_BUMP_VERSION_TASK_NAME)
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

    private void setupTestingGitRepo() {
        project.exec ReleaseUtil.git("init", projectDir.getAbsolutePath())
        project.exec ReleaseUtil.git("add", ".")
        project.exec ReleaseUtil.git("commit", "-m", "initial commit")
    }

    private void checkForTaskSuccess(String taskName, Closure closure) {
        checkForTaskSuccess(taskName)
        closure.call()
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
