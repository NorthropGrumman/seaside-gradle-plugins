package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

@SuppressWarnings("deprecation")
class SeasideBatsPluginTest {

    private SeasideBatsPlugin plugin
    private Project project

    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply(SeasideBatsPlugin)
    }

    @Test
    void appliesPlugin() {
        Assert.assertNotNull(project.extensions.findByName(SeasideBatsPlugin.BATS_EXTENSION_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideBatsPlugin.EXTRACT_BATS_TASK_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideBatsPlugin.RUN_BATS_TASK_NAME))
    }

}
