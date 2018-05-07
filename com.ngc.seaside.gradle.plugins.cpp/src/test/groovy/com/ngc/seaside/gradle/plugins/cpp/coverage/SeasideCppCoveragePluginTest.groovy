package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideCppCoveragePluginTest {

    private Project project

    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply(SeasideCppCoveragePlugin)
    }

    @Test
    void appliesPlugin() {
        Assert.assertNotNull(project.extensions.findByName(SeasideCppCoveragePlugin.CPP_COVERAGE_EXTENSION_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_COVERAGE_DATA_TASK_NAME))
        Assert.assertNotNull(
                TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_CPPCHECK_REPORT_TASK_NAME))
        Assert.assertNotNull(TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_RATS_REPORT_TASK_NAME))
        Assert.assertNotNull(
                TaskResolver.findTask(project, SeasideCppCoveragePlugin.GENERATE_FULL_COVERAGE_REPORT_TASK_NAME))
    }
}
