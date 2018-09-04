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
package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.when

class CreateCelixRunScriptTaskIT {

    private CreateCelixRunScriptTask task

    private File file

    private ProjectInternal project = GradleMocks.newProjectMock()

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder()

    @Before
    void setup() {
        file = outputDirectory.newFile()

        when(project.file(anyString())).thenReturn(file)

        // If you don't create the task like this, Gradle blows up.
        task = new TaskBuilder<CreateCelixRunScriptTask>(CreateCelixRunScriptTask)
              .setProject(project)
              .create()
        task.scriptFile = file
    }

    @Test
    void doesGenerateDefaultScript() {
        task.createRunScript()
        assertTrue("file not created!",
                   file.isFile())
    }

    @Test
    void doesGenerateCustomScript() {
        task.scriptTemplate = "hello world"
        task.createRunScript()
        assertTrue("file not created!",
                   file.isFile())
        assertEquals("file contents not correct!",
                     Collections.singletonList(task.scriptTemplate),
                     file.readLines())
    }
}
