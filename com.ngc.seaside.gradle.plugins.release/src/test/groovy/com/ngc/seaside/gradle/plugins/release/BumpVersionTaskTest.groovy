/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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

import static org.mockito.Mockito.*

import com.ngc.seaside.gradle.plugins.release.task.BumpVersionTask
import com.ngc.seaside.gradle.plugins.version.IVersionUpgradeStrategy
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder

import org.gradle.api.internal.project.ProjectInternal
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test for the BumpVersionTask
 */
@RunWith(MockitoJUnitRunner.Silent)
class BumpVersionTaskTest {
    private static final String TEST_VERSION_SUFFIX = "-SNAPSHOT"
    private static final String TEST_UPGRADE_VERSION = "1.2.2"
    private static final String TEST_RELEASE_PATCH_VERSION = "1.2.3"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.3.0"
    private static final String TEST_RELEASE_MAJOR_VERSION = "2.0.0"

    private ProjectInternal testProject
    private BumpVersionTask task

    @Mock
    private VersionResolver resolver

    @Mock
    private IVersionUpgradeStrategy upgradeStrategy

    @Before
    void before() {
        testProject = GradleMocks.newProjectMock()
        when(testProject.rootProject).thenReturn(testProject)
        when(testProject.rootProject.hasProperty("releaseVersion")).thenReturn(true)
        when(resolver.getProjectVersion()).thenReturn(TEST_UPGRADE_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.PATCH)).thenReturn(TEST_RELEASE_PATCH_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.MINOR)).thenReturn(TEST_RELEASE_MINOR_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.MAJOR)).thenReturn(TEST_RELEASE_MAJOR_VERSION)
    }

    @Test
    void canUpgradeVersionForPatchRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.PATCH, TEST_RELEASE_PATCH_VERSION + TEST_VERSION_SUFFIX)
    }

    @Test
    void canUpgradeVersionForMinorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MINOR, TEST_RELEASE_MINOR_VERSION + TEST_VERSION_SUFFIX)
    }

    @Test
    void canUpgradeVersionForMajorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MAJOR, TEST_RELEASE_MAJOR_VERSION + TEST_VERSION_SUFFIX)
    }

    private void confirmVersionUpgradeForReleaseType(ReleaseType type, String expectedUpgradeVersion) {
        task = createTaskWithReleaseType(type)
        Assert.assertEquals(
                "the current version should have been: " + TEST_UPGRADE_VERSION,
                TEST_UPGRADE_VERSION,
                task.getCurrentVersion()
        )

        task.getVersionAfterRelease()
        verify(resolver).getUpdatedProjectVersionForRelease(type)
        Assert.assertEquals(
                "The release type should have been: $type",
                type,
                task.getReleaseType()
        )

        Assert.assertEquals(
                "This was the version expected: $expectedUpgradeVersion",
                expectedUpgradeVersion,
                task.getVersionAfterRelease())
    }

    private BumpVersionTask createTaskWithReleaseType(ReleaseType type) {
        return new TaskBuilder<BumpVersionTask>(BumpVersionTask)
              .setProject(testProject)
              .setName(SeasideReleaseRootProjectPlugin.RELEASE_BUMP_VERSION_TASK_NAME)
              .setSupplier({ new BumpVersionTask(resolver, type) })
              .create()
    }

}
