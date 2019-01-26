/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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
