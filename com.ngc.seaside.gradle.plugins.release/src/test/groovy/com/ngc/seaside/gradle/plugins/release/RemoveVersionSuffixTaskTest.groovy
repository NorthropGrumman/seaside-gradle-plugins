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

import static org.mockito.Mockito.when

import com.ngc.seaside.gradle.plugins.release.task.RemoveVersionSuffixTask
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.util.Versions
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent)
class RemoveVersionSuffixTaskTest {
    private static final String TEST_UPGRADE_VERSION = "1.2.3"
    private static final String TEST_VERSION_FROM_FILE = "${TEST_UPGRADE_VERSION}${Versions.VERSION_SUFFIX}"

    private RemoveVersionSuffixTask task

    @Mock
    private VersionResolver resolver

    @Before
    void before() {
        when(resolver.getProjectVersion()).thenReturn(TEST_VERSION_FROM_FILE)

        task = new TaskBuilder<RemoveVersionSuffixTask>(RemoveVersionSuffixTask)
              .setProject(GradleMocks.newProjectMock())
              .setName(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME)
              .setSupplier({ new RemoveVersionSuffixTask(resolver) })
              .create()

    }

    @Test
    void versionSuffixIsIncludedWithCurrentVersion() {
        Assert.assertEquals(
              "The current version should end with the version suffix!",
              task.getCurrentVersion(),
              TEST_VERSION_FROM_FILE
        )
    }

    @Test
    void versionSuffixIsNotIncludedWithVersionForRelease() {
        Assert.assertEquals(
              "The release version should not end with the version suffix!",
              task.getVersionForRelease(),
              TEST_UPGRADE_VERSION
        )
    }
}
