package com.ngc.seaside.gradle.util.test

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskDependency
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
     * Creates a new Gradle {@code Project} that contains a mocked service registry and configuration container.
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

        ConfigurationContainer configs = mock(ConfigurationContainer)
        when(project.getConfigurations()).thenReturn(configs)

        return project
    }

    static Configuration newConfiguration(String name) {
        Configuration config = mock(Configuration)
        when(config.getName()).thenReturn(name)
        when(config.getDependencies()).thenReturn(new MockedDependencySet())
        return config
    }

    private static class MockedDependencySet extends DefaultDomainObjectSet implements DependencySet {
        private MockedDependencySet() {
            super(Dependency)
        }

        @Override
        TaskDependency getBuildDependencies() {
            throw new UnsupportedOperationException("not implemented")
        }
    }
}
