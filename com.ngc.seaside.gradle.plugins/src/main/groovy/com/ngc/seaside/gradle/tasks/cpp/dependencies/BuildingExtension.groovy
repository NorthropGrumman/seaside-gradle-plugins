package com.ngc.seaside.gradle.tasks.cpp.dependencies

import org.gradle.api.Project

/**
 *
 */
class BuildingExtension {

    List<String> headers = []
    BuildingExtensionDataStore storage = new BuildingExtensionDataStore()
    Project project

    BuildingExtension(Project project) {
        this.project = project
    }

    void api(String dependencyName) {
      storage.addApi(dependencyName)
    }

    void statically(String dependencyName) {
        statically(dependencyName, null)
    }

    void statically(Closure closure) {
        statically("", closure)
    }

    void statically(String dependencyName, Closure closure) {
        StaticBuildConfiguration statically = new StaticBuildConfiguration(this.project)
        statically.dependency = dependencyName
        if (closure != null) {
            project.configure(statically, closure)
        }
        storage.add(statically)
    }

    void shared(String dependencyName) {
        shared(dependencyName, null)
    }

    void shared(Closure closure) {
        shared("", closure)
    }

    void shared(String dependencyName, Closure closure) {
        SharedBuildConfiguration shared = new SharedBuildConfiguration(this.project)
        shared.dependency = dependencyName
        if (closure != null) {
            project.configure(shared, closure)
        }
        storage.add(shared)
    }

}
