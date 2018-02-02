package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideReleaseRootProjectPluginFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath
    private String projectName = "bonjourlemonde"

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
    void doesRemoveVersionSuffix() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--pretty=format:%s")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertTrue(
                  "output did not contain expected release message!",
                  output.toString().split("\n").contains("Release of version v1.2.3")
            )
        }
    }

    @Test
    void doesCreateTag() {
        // The version suffix will always need to be removed before creating the tag.
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "tag", "--list")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertTrue(
                  "output did not contain expected release git tag!",
                  output.toString().split("\n").contains("v1.2.3")
            )
        }
    }

    @Test
    void doesBumpVersion() {
        // The version suffix will always need to be removed before bumping the version.
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_BUMP_VERSION_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--pretty=format:%s")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertTrue(
                  "output did not contain the expected update version message!",
                  output.toString().split("\n").contains("Creating new 1.3.0-SNAPSHOT version after release")
            )
        }
    }

    @Test
    void doesReleasePush() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_PUSH_TASK_NAME)
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "sealion-java-hello-world-monorepo"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "release", "sealion-java-hello-world-monorepo"
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
                .withProjectDir(TestingUtilities.turnListIntoPath(projectDir.toString(), projectName))
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build", taskName)
                .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName").outcome)
    }
}
