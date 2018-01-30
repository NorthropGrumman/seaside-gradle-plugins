package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.VersionResolver
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent)
class UpdateVersionTaskTest {
    private static final String TEST_UPGRADE_VERSION = "1.2.3"
    private static final String TEST_VERSION_FROM_FILE = "${TEST_UPGRADE_VERSION}${VersionResolver.VERSION_SUFFIX}"

    private UpdateVersionTask task

    @Mock
    private VersionResolver resolver

    @Before
    void before() {
        when(resolver.getProjectVersion()).thenReturn(TEST_VERSION_FROM_FILE)

        task = new TaskBuilder<UpdateVersionTask>(UpdateVersionTask)
              .setProject(GradleMocks.newProjectMock())
              .setName(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
              .setSupplier({ new UpdateVersionTask(resolver) })
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
