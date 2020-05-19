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

class SeasideReleaseRootProjectPluginIT {
    private static final String VERSION_GRADLE_TEST_VERSION = "1.2.3-SNAPSHOT"

    private File projectDir
    private Project project
    private TaskResolver resolver
    private SeasideReleaseRootProjectPlugin plugin
    private List<String> taskNames = [
          SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME,
          SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME,
          SeasideReleaseRootProjectPlugin.RELEASE_PUSH_TASK_NAME,
          SeasideReleaseRootProjectPlugin.RELEASE_BUMP_VERSION_TASK_NAME
    ]

    @Before
    void before() {
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
                sourceDirectoryWithTheTestProject(),
                pathToTheDestinationProjectDirectory()
        )

        // We would only ever apply this plugin to projects within a "mono repo" containing various other projects.
        project = TestingUtilities.createTheTestProjectWith(
              TestingUtilities.turnListIntoPath(projectDir.toString(), "bonjourlemonde")
        )
        applyThePlugin()
    }

    @Test
    void doesApplyPluginUsingVersionFile() {
        Assert.assertEquals(VERSION_GRADLE_TEST_VERSION, project.version.toString())
        Assert.assertNotNull(project.extensions.findByName(SeasideReleaseRootProjectPlugin.RELEASE_ROOT_PROJECT_EXTENSION_NAME))
        verifyTasksExistOnThePlugin()
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
                "src", "integrationTest", "resources", "sealion-java-hello-world-monorepo"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
                "build", "integrationTest", "resources", "release", "sealion-java-hello-world-monorepo"
        )
    }

    private void applyThePlugin() {
        plugin = new SeasideReleaseRootProjectPlugin()
        plugin.apply(project)

        project.extensions
              .findByName(SeasideReleaseRootProjectPlugin.RELEASE_ROOT_PROJECT_EXTENSION_NAME)
              .uploadArtifacts = false

        resolver = new TaskResolver(project)
    }

    private void verifyTasksExistOnThePlugin() {
        taskNames.each { taskNames ->
            Assert.assertNotNull(resolver.findTask(taskNames))
        }
    }
}
