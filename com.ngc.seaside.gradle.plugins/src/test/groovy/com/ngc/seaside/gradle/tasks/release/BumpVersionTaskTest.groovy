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

import static org.mockito.Mockito.*


/**
 * Test for the BumpVersionTask
 */
@RunWith(MockitoJUnitRunner.Silent)
class BumpVersionTaskTest {
    private static final String TEST_VERSION_SUFFIX = "-SNAPSHOT"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.2.2"
    private static final String TEST_RELEASE_NEW_VERSION = "1.3.0"

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
        when(resolver.getProjectVersion()).thenReturn(TEST_RELEASE_MINOR_VERSION)
    }

    @Test
    void canUpgradeVersionForMinorRelease() {
        confirmVersionUpgradeForReleaseType(TEST_RELEASE_NEW_VERSION + TEST_VERSION_SUFFIX)
    }

    private void confirmVersionUpgradeForReleaseType(String expectedUpgradeVersion) {
        task = createTaskWithReleaseType()
        task.setNextVersion()
        verify(resolver).getProjectVersion()
        Assert.assertEquals(expectedUpgradeVersion, task.setNextVersion())
    }

    private BumpVersionTask createTaskWithReleaseType() {
        return new TaskBuilder<BumpVersionTask>(BumpVersionTask)
              .setProject(testProject)
              .setName(SeasideReleaseMonoRepoPlugin.RELEASE_BUMP_VERSION_TASK_NAME)
              .setSupplier({ new BumpVersionTask(resolver) })
              .create()
    }
}
