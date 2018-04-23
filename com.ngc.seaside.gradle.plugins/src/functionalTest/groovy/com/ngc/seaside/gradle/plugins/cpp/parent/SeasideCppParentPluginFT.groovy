package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assume.assumeFalse

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class SeasideCppParentPluginFT {


    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        Path targetPath = Paths.get("build/functionalTest/cpp/parent/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build")
                .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:build").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:build").getOutcome())
    }

    @Test
    void doesRunGradleAnalyzeBuildWithSuccess() {

        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("analyze")
                .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.api:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.utilities:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.logservice:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.thread.impl.threadservice:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.time.impl.timeservice:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.event.impl.synceventservice:analyze").getOutcome())
        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.log.impl.printservice:analyze").getOutcome())
    }
}
