package com.ngc.seaside.gradle.plugins.util

import com.ngc.seaside.gradle.api.IResolver
import org.gradle.api.Project
import org.gradle.api.Task

class TaskResolver implements IResolver{

    private Project project

    TaskResolver(Project project) {
        this.project = project
    }

    Task findTask(String taskName) {
        return project.tasks.getByName(taskName)
    }

    static Task findTask(Project project, String taskName) {
        return project.tasks.getByName(taskName)
    }

    Task findTask(String taskName, Closure closure) {
        return this.project.tasks.getByName(taskName, closure)
    }

    static Task findTask(Project project, String taskName, Closure closure) {
        return project.tasks.getByName(taskName, closure)
    }
}
