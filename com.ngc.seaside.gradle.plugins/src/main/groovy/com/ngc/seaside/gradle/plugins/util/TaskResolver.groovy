package com.ngc.seaside.gradle.plugins.util

import org.gradle.api.Project
import org.gradle.api.Task

class TaskResolver {

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
        return this.project.tasks.getByName(taskName)
    }

    static Task findTask(Project project, String taskName, Closure closure) {
        return project.tasks.getByName(taskName)
    }
}