package com.ngc.seaside.gradle.util.test

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assume

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
        def taskOutcome = result.task(":$projectName:$taskName").outcome

        Assert.assertTrue(
              "unexpected task outcome!",
              taskOutcome == TaskOutcome.SUCCESS || taskOutcome == TaskOutcome.UP_TO_DATE
        )
    }

    static assertFilePathsSame(String message, String expected, String actual) {
        Assert.assertEquals(message, Paths.get(expected), Paths.get(actual))
    }

    static void tryToConnectToUrl(String urlString) {
        def url = new URL(urlString)
        try {
            def connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 1000
            connection.connect()
            Assume.assumeTrue(connection.responseCode < 400)
        } catch (Exception e) {
            Assume.assumeNoException(e)
        }
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
