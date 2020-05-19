/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
