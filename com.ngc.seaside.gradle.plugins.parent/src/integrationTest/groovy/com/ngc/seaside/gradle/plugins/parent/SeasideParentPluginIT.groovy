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
package com.ngc.seaside.gradle.plugins.parent

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideParentPluginIT {

    private File projectDir
    private Project project
    private SeasideParentPlugin plugin

    @Before
    void before() {
        File source = Paths.get("src/integrationTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/integrationTest/resources/parent/com.ngc.example.parent")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        plugin = new SeasideParentPlugin()

        setRequiredProjectProperties(project)
        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        TaskResolver resolver = new TaskResolver(project)
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.SOURCE_JAR_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.JAVADOC_JAR_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.ANALYZE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.DOWNLOAD_DEPENDENCIES_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.CLEANUP_DEPENDENCIES_TASK_NAME))
    }

    static void setRequiredProjectProperties(Project project) {
        String test = "test"
        project.ext.nexusReleases = test
        project.ext.nexusUsername = test
        project.ext.nexusPassword = test
        project.ext.nexusSnapshots = test
        project.ext.nexusConsolidated = test
    }
}
