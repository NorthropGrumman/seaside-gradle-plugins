package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.plugins.release.SeasideReleaseMonoRepoPlugin
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateVersionTaskTest {
    private static final String TEST_RELEASE_VERSION = "1.2.3"

    private UpdateVersionTask task

    @Before
    void before() {
        task = new TaskBuilder<UpdateVersionTask>(UpdateVersionTask)
            .setProject(GradleMocks.newProjectMock())
            .setName(SeasideReleaseMonoRepoPlugin.RELEASE_UPDATE_VERSION_TASK_NAME)
            .create()
    }

    @Test
    void canPerformSnapshotRelease() {
        task.prepareForSnapshotRelease()
        Assert.assertEquals(ReleaseType.SNAPSHOT, task.getReleaseType())
    }
}
