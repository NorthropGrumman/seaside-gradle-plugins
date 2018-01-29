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

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when


/**
 * Test for the CreateTagTask
 */
@RunWith(MockitoJUnitRunner.Silent)
class CreateTagTaskTest {
    private static final String TEST_PREFIX ="v"
    private static final String TEST_UPGRADE_VERSION = "1.2.2"
    private static final String TEST_RELEASE_PATCH_VERSION = "1.2.3"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.3.0"
    private static final String TEST_RELEASE_MAJOR_VERSION = "2.0.0"

    private ProjectInternal testProject
    private CreateTagTask task

    @Mock
    private IVersionUpgradeStrategy upgradeStrategy

    @Mock
    private VersionResolver resolver

    @Before
    void before() {
        testProject = GradleMocks.newProjectMock()
        when(testProject.rootProject).thenReturn(testProject)
        when(testProject.rootProject.hasProperty("releaseVersion")).thenReturn(true)

        when(resolver.getProjectVersion(any())).thenReturn(TEST_UPGRADE_VERSION)


    }

    @Test
    void canUpgradeVersionForPatchRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.PATCH, TEST_PREFIX + TEST_RELEASE_PATCH_VERSION)
    }

    @Test
    void canUpgradeVersionForMinorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MINOR, TEST_PREFIX + TEST_RELEASE_MINOR_VERSION)
    }

    @Test
    void canUpgradeVersionForMajorRelease() {
        confirmVersionUpgradeForReleaseType(ReleaseType.MAJOR, TEST_PREFIX + TEST_RELEASE_MAJOR_VERSION)
    }

    private void confirmVersionUpgradeForReleaseType(ReleaseType releaseType, String expectedUpgradeVersion) {
        task = createTask(releaseType)
        task.setTagName()
        verify(resolver).getProjectVersion(releaseType)
        Assert.assertEquals(expectedUpgradeVersion, task.getTagName())
    }

    private CreateTagTask createTask(ReleaseType releaseType) {
        return new TaskBuilder<CreateTagTask>(CreateTagTask)
              .setProject(testProject)
              .setName(SeasideReleaseMonoRepoPlugin.RELEASE_CREATE_TAG_TASK_NAME)
              .setSupplier({ new CreateTagTask(resolver, releaseType, TEST_PREFIX) })
              .create()
    }
}
