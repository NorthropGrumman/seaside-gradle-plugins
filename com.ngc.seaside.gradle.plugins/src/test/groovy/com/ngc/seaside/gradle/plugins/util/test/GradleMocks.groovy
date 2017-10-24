package com.ngc.seaside.gradle.plugins.util.test

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.service.ServiceRegistry
import org.gradle.util.Path

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * Contains factory methods for creating mocked Gradle API elements.
 */
class GradleMocks {

    private GradleMocks() {
    }

    /**
     * Creates a new Gradle {@code Project} that contains a mocked service registry.
     */
    static ProjectInternal newProjectMock() {
        // We have to reference the internal Gradle API ProjectInternal because that is what is needed to
        // create tasks outside of the DSL.
        ServiceRegistry registry = mock(ServiceRegistry)

        Path projectPath = Path.ROOT

        ProjectInternal project = mock(ProjectInternal)
        when(project.getServices()).thenReturn(registry)
        when(project.getProjectPath()).thenReturn(projectPath)
        when(project.getIdentityPath()).thenReturn(projectPath)

        return project
    }
}
