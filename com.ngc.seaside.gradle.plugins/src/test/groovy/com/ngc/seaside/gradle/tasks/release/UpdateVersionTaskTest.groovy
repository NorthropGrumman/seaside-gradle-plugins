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

import static org.mockito.Mockito.any
import static org.mockito.Mockito.when
import static org.mockito.Mockito.verify

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

        when(resolver.getProjectVersion(any())).thenReturn(TEST_UPGRADE_VERSION)
        when(resolver.getProjectVersion(ReleaseType.SNAPSHOT)).thenReturn(TEST_RELEASE_SNAPSHOT_VERSION)
    }

    @Test(expected = IllegalStateException.class)
    void doesFailIfReleaseVersionPropertyIsNotSetOnProject() {
        when(testProject.rootProject.hasProperty("releaseVersion")).thenReturn(false)
        task = createTaskWithReleaseType(ReleaseType.PATCH)
        task.updateReleaseVersion()
    }

    @Test
    void doesNotUpgradeVersionForSnapshotRelease() {
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
        task.updateReleaseVersion()
        verify(resolver).getProjectVersion(releaseType)
        Assert.assertEquals(releaseType, task.getReleaseType())
        Assert.assertEquals(expectedUpgradeVersion, task.getVersionForRelease())
    }

    private UpdateVersionTask createTaskWithReleaseType(ReleaseType releaseType) {
        return new TaskBuilder<UpdateVersionTask>(UpdateVersionTask)
              .setProject(testProject)
              .setName(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
              .setSupplier({ new UpdateVersionTask(resolver, releaseType) })
              .create()
    }
}
