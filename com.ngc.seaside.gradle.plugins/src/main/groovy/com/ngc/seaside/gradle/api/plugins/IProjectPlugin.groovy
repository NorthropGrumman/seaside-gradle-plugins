package com.ngc.seaside.gradle.api.plugins

import com.ngc.seaside.gradle.util.TaskResolver
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.Plugin
import org.gradle.api.Project

interface IProjectPlugin extends Plugin<Project> {

    @Override
    void apply(Project project)

    /**
     * Default action for applying a project plugin
     * @param project project applying this plugin
     */
    void doApply(Project project)

    /**
     * @return instance of TaskResolver instance from parent
     */
    TaskResolver getTaskResolver()

    /**
     * @return instance of VersionResolver instance from parent
     */
    VersionResolver getVersionResolver()
}
