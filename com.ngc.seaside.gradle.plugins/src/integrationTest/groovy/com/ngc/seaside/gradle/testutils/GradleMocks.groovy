package com.ngc.seaside.gradle.testutils

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.service.ServiceRegistry

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GradleMocks {

    private GradleMocks() {
    }

    static ProjectInternal newProjectMock() {
        ServiceRegistry registry = mock(ServiceRegistry)

        ProjectInternal project = mock(ProjectInternal)
        when(project.getServices()).thenReturn(registry)

        return project
    }
}
