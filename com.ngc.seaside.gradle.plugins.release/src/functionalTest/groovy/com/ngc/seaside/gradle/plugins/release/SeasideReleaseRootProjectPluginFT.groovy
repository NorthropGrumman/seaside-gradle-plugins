/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideReleaseRootProjectPluginFT {
    private File projectDir
    private Project project
    private String projectName = "bonjourlemonde"

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
        setupTestingGitRepo()
    }

    @After
    void after() {
        projectDir.deleteDir()
    }

    @Test
    void doesRemoveVersionSuffix() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--format=%s")
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
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--format=%s")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertTrue(
                  "output did not contain the expected update version message!",
                  output.toString().split("\n").contains("Creating new 1.3.0-SNAPSHOT version after release")
            )
        }
    }

    @Test
    void doesReleasePush() {
        def remoteRepo = TestingUtilities.turnListIntoPath(projectDir.parentFile.absolutePath, "test")
        setupTestingGitRemote(remoteRepo)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_BUMP_VERSION_TASK_NAME)
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_PUSH_TASK_NAME) {
            assertCorrectTagWasPushed()
            assertCorrectCommitWasPushed()
        }
        remoteRepo.deleteDir()
    }

    private void assertCorrectTagWasPushed() {
        def output = new ByteArrayOutputStream()
        def result = project.exec ReleaseUtil.gitWithOutput(output, "ls-remote", "origin", "refs/tags/v1.2.3")
        Assert.assertEquals(0, result.getExitValue())
        Assert.assertTrue(
              "the tag wasn't pushed to the remote repo!",
              output.toString().trim().endsWith("refs/tags/v1.2.3")
        )
    }

    private void assertCorrectCommitWasPushed() {
        Assert.assertEquals(
              "the last commit in origin does not match what is in origin!",
              getLocalCommitHash(),
              getRemoteCommitHash()
        )
    }

    private String getLocalCommitHash() {
        def output = new ByteArrayOutputStream()
        def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--format=%H", "-n1")
        Assert.assertEquals(0, result.getExitValue())
        return output.toString().trim()
    }

    private String getRemoteCommitHash() {
        def output = new ByteArrayOutputStream()
        def result = project.exec ReleaseUtil.gitWithOutput(output, "ls-remote", "origin", "HEAD")
        Assert.assertEquals(0, result.getExitValue())
        return output.toString().trim().split("\t")[0]
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
        project.exec ReleaseUtil.git("init", projectDir.absolutePath)
        project.exec ReleaseUtil.git("config", "--local", "core.hookspath", "\"\"")
        project.exec ReleaseUtil.git("add", ".")
        project.exec ReleaseUtil.git("commit", "-m", "initial commit")
    }

    private void setupTestingGitRemote(File remoteRepo) {
        if (remoteRepo.exists()) {
            remoteRepo.deleteDir()
        }

        project.exec ReleaseUtil.git("init", "--bare", remoteRepo.absolutePath)
        project.exec ReleaseUtil.git("remote", "add", "origin", remoteRepo.absolutePath)
    }

    private void checkForTaskSuccess(String taskName, Closure closure) {
        checkForTaskSuccess(taskName)
        closure.call()
    }

    private void checkForTaskSuccess(String taskName) {
        BuildResult result = SeasideGradleRunner.create()
                .withNexusProperties()
                .withProjectDir(TestingUtilities.turnListIntoPath(projectDir.toString(), projectName))
                .withPluginClasspath()
                .forwardOutput()
                .withArguments("clean", "build", taskName)
                .build()

        Assert.assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName").outcome)
    }
}
