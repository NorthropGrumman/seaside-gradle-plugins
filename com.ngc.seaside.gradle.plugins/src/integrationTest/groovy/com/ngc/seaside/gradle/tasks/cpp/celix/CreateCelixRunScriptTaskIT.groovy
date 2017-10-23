package com.ngc.seaside.gradle.tasks.cpp.celix

import com.ngc.seaside.gradle.testutils.GradleMocks
import com.ngc.seaside.gradle.testutils.TaskBuilder
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
