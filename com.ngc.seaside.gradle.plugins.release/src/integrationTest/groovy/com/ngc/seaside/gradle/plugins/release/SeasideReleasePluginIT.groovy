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

import com.ngc.seaside.gradle.util.TaskResolver
import com.ngc.seaside.gradle.util.test.TestingUtilities

import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Paths

class SeasideReleasePluginIT {
    private static final String BUILD_GRADLE_TEST_VERSION_NUMBER = "1.2.3-SNAPSHOT"
    private static final String VERSIONS_GRADLE_TEST_VERSION_NUMBER = "1.2.4-SNAPSHOT"

    private File projectDir
    private Project project
    private TaskResolver resolver
    private SeasideReleasePlugin plugin

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)
    }

    @Test
    void doesApplyPlugin() {
        makeTheVersionResolverUseTheDefaultVersionFile()
        applyThePlugin()

        Assert.assertEquals(BUILD_GRADLE_TEST_VERSION_NUMBER, project.version.toString())

        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))

        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX))
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "integrationTest", "resources", "sealion-java-hello-world"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "integrationTest", "resources", "release", "sealion-java-hello-world"
        )
    }

    private void makeTheVersionResolverUseTheDefaultVersionFile() {
        Paths.get(projectDir.toString(), "versions.gradle").toFile().delete()
    }

    private void applyThePlugin() {
        plugin = new SeasideReleasePlugin()
        plugin.apply(project)

        project.extensions
              .findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME)
              .uploadArtifacts = false

        project.evaluate()
        resolver = new TaskResolver(project)
    }
}
