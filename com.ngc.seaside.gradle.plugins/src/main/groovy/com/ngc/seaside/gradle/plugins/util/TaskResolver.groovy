package com.ngc.seaside.gradle.plugins.util

import org.gradle.api.Project
import org.gradle.api.Task


public class TaskResolver {

    private Project project

    TaskResolver(Project project) {
        this.project = project
    }

    public Task findTask(Project project = this.project, String taskName) {
        return project.tasks.getByName(taskName)
    }

    public Task findTask(Project project = this.project, String taskName, Closure) {
        return project.tasks.getByName(taskName)
    }
}
