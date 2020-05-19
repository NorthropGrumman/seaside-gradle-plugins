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
