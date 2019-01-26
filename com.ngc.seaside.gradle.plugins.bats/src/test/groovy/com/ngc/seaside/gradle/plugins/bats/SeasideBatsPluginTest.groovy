/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
