package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.VersionResolver
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent)
class UpdateVersionTaskTest {
    private static final String TEST_VERSION_SUFFIX = "-SNAPSHOT"
    private static final String TEST_UPGRADE_VERSION = "1.2.2"
    private static final String TEST_RELEASE_SNAPSHOT_VERSION = "${TEST_UPGRADE_VERSION}${TEST_VERSION_SUFFIX}"
    private static final String TEST_RELEASE_PATCH_VERSION = "1.2.3"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.3.0"
    private static final String TEST_RELEASE_MAJOR_VERSION = "2.0.0"

    private ProjectInternal testProject
    private UpdateVersionTask task

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
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.SNAPSHOT)).thenReturn(TEST_RELEASE_SNAPSHOT_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.PATCH)).thenReturn(TEST_RELEASE_PATCH_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.MINOR)).thenReturn(TEST_RELEASE_MINOR_VERSION)
        when(resolver.getUpdatedProjectVersionForRelease(ReleaseType.MAJOR)).thenReturn(TEST_RELEASE_MAJOR_VERSION)
    }

    @Test
    void doesNotUpgradeVersionForSnapshotRelease() {
        when(resolver.getProjectVersion()).thenReturn(TEST_RELEASE_SNAPSHOT_VERSION)
        confirmVersionUpgradeForReleaseType(ReleaseType.SNAPSHOT, TEST_RELEASE_SNAPSHOT_VERSION)
    }

    @Test
    void canUpgradeVersionForPatchRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.PATCH, TEST_RELEASE_PATCH_VERSION)
    }

    @Test
    void canUpgradeVersionForMinorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MINOR, TEST_RELEASE_MINOR_VERSION)
    }

    @Test
    void canUpgradeVersionForMajorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MAJOR, TEST_RELEASE_MAJOR_VERSION)
    }

    private void confirmVersionUpgradeForReleaseType(ReleaseType releaseType, String expectedUpgradeVersion) {
        task = createTaskWithReleaseType(releaseType)
        Assert.assertEquals(
              "the current version should have been: " + expectedCurrentVersion(),
              expectedCurrentVersion(),
              task.getCurrentVersion()
        )

        task.updateReleaseVersion()
        verify(resolver).getUpdatedProjectVersionForRelease(releaseType)

        Assert.assertEquals(
              "the release type should have been: $releaseType",
              releaseType,
              task.getReleaseType()
        )
        Assert.assertEquals(
              "the version to release should have been: $expectedUpgradeVersion",
              expectedUpgradeVersion,
              task.getVersionForRelease()
        )
    }

    private UpdateVersionTask createTaskWithReleaseType(ReleaseType releaseType) {
        return new TaskBuilder<UpdateVersionTask>(UpdateVersionTask)
              .setProject(testProject)
              .setName(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
              .setSupplier({ new UpdateVersionTask(resolver, releaseType) })
              .create()
    }

    private String expectedCurrentVersion() {
        // If we try to perform a snapshot release, nothing should change. (Don't ask me why we even have "snapshot
        // releases", though...)
        return (task.getReleaseType() == ReleaseType.SNAPSHOT) ?
                TEST_RELEASE_SNAPSHOT_VERSION :
                TEST_UPGRADE_VERSION
    }
}
