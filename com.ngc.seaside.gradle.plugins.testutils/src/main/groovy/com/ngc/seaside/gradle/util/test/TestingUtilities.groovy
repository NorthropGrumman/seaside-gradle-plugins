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
package com.ngc.seaside.gradle.util.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import java.nio.file.Files
import java.nio.file.Paths

class TestingUtilities {

    static File setUpTheTestProjectDirectory(File sourcePath, File destPath) {
        def testProjectDir = createTheTestProjectDirectory(destPath)
        copyTheTestProjectIntoTheTestProjectDirectory(sourcePath, testProjectDir)
        return testProjectDir
    }

    static Project createTheTestProjectWith(File testProjectDir) {
        return ProjectBuilder.builder().withProjectDir(testProjectDir).build()
    }

    static Project createSubprojectWithDir(Project parent, File dir) {
        return ProjectBuilder
            .builder()
            .withParent(parent)
            .withName(dir.name)
            .withProjectDir(dir)
            .build()
    }

    static File turnListIntoPath(String... list) {
        return Paths.get(list.flatten().join(File.separator)).toFile()
    }

    static void assertTaskSuccess(BuildResult result, String projectName, String taskName) {
        def taskOutcome
        if (projectName == null) {
           taskOutcome = result.task(":$taskName").outcome
        } else {
           taskOutcome = result.task(":$projectName:$taskName").outcome
        }
        Assert.assertTrue(
              "unexpected task outcome!",
              taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE
        )
    }

    static assertFilePathsSame(String message, String expected, String actual) {
        Assert.assertEquals(message, Paths.get(expected), Paths.get(actual))
    }

    static boolean tryToConnectToUrl(String urlString) {
        def url = new URL(urlString)
        try {
            def connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 1000
            connection.connect()
            return connection.responseCode < 400
        } catch (Exception e) {
        }
        return false
    }

    private static URL getThePluginClassPathResource(Class c) {
        return c.classLoader.getResource("plugin-classpath.txt")
    }

    private static void throwIfTheClasspathResourceIsNotFound(URL r) {
        if (!r)
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
    }

    private static List<File> createNewFileForEachItemInClasspath(URL r) {
        return r.readLines().collect { new File(it) }
    }

    private static File createTheTestProjectDirectory(File dir) {
        return Files.createDirectories(dir.toPath()).toFile()
    }

    private static void copyTheTestProjectIntoTheTestProjectDirectory(File source, File destination) {
        FileUtils.copyDirectory(source, destination)
    }
}
