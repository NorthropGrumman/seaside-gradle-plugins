package com.ngc.seaside.gradle.tasks.eclipse.feature

import com.ngc.seaside.gradle.plugins.eclipse.feature.SeasideEclipseFeaturePlugin
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.gradle.api.DefaultTask
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CreateJarTaskTest {
    private CreateJarTask task

    @Before
    void before() {
        task = new TaskBuilder<CreateJarTask>(CreateJarTask)
              .setProject(GradleMocks.newProjectMock())
              .setName(SeasideEclipseFeaturePlugin.ECLIPSE_CREATE_JAR_TASK_NAME)
              .create()
    }

    @Test
    void taskExtendsDefaultTask() {
        Assert.assertTrue(
              "createJar task does not extend DefaultTask!",
              task instanceof DefaultTask
        )
    }
}
