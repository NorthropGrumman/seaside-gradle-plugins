package com.ngc.seaside.gradle.api

import com.ngc.seaside.gradle.plugins.util.TaskResolver
import com.ngc.seaside.gradle.plugins.util.VersionResolver
import org.gradle.api.Project

/**
 * This base class for a project project plugin implementation is used to
 * ensure that the project version setting remains the same amongst all plugins.
 * This also allows plugins to be implemented with knowledge that the project.version is set correctly.
 */
abstract class AbstractProjectPlugin implements IProjectPlugin {

    private VersionResolver versionResolver
    private TaskResolver taskResolver

    /**
     * Inject project version configuration and force subclasses to use it
     * @param project project applying this plugin
     */
    @Override
    final void apply(Project project) {
        this.taskResolver = new TaskResolver(project)
        this.versionResolver = new VersionResolver(project)
        project.configure(project) {
            project.version = versionResolver.getProjectVersion()
        }
        doApply(project)
    }

    @Override
    TaskResolver getTaskResolver() {
        return this.taskResolver
    }

    @Override
    VersionResolver getVersionResolver() {
        return this.versionResolver
    }
}
