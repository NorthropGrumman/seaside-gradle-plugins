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
package com.ngc.seaside.gradle.plugins.cpp.parent

import static org.junit.Assume.assumeFalse

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner

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

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class SeasideCppParentPluginFT {


    private File projectDir
    private Project project

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        File source = Paths.get("src/functionalTest/resources/pipeline-test-cpp").toFile()
        Path targetPath = Paths.get("build/functionalTest/cpp/parent/pipeline-test-cpp")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
                .withPluginClasspath()
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
                .withPluginClasspath()
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
