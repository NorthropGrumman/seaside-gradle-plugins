/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
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
