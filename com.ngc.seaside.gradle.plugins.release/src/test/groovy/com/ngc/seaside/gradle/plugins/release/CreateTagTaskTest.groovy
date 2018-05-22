package com.ngc.seaside.gradle.plugins.release

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import com.ngc.seaside.gradle.plugins.release.task.CreateTagTask
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
 * Test for the CreateTagTask
 */
@RunWith(MockitoJUnitRunner.Silent)
class CreateTagTaskTest {
    private static final String TEST_PREFIX ="v"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.3.0"

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
        when(resolver.getProjectVersion()).thenReturn(TEST_RELEASE_MINOR_VERSION)

    }

    @Test
    void canUpgradeVersionForPatchRelease() {
        confirmVersionUpgradeForReleaseType(TEST_PREFIX + TEST_RELEASE_MINOR_VERSION)
    }

    private void confirmVersionUpgradeForReleaseType(String expectedUpgradeVersion) {
        task = createTask()
        task.setTagName()
        verify(resolver).getProjectVersion()
        Assert.assertEquals(expectedUpgradeVersion, task.getTagName())
    }

    private CreateTagTask createTask() {
        return new TaskBuilder<CreateTagTask>(CreateTagTask)
              .setProject(testProject)
              .setName(SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME)
              .setSupplier({ new CreateTagTask(resolver, TEST_PREFIX) })
              .create()
    }
}