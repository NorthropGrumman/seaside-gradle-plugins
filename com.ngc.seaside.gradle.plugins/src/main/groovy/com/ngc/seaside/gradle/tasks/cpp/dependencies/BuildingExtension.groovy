package com.ngc.seaside.gradle.tasks.cpp.dependencies

import org.gradle.api.Project

/**
 * Created by jprovence on 9/27/2017.
 */
class BuildingExtension {

    List<String> headers = []
    List<Statically> staticList = []
    Store storage = new Store()

    Project project

    BuildingExtension(Project project) {
        this.project = project
    }

    void statically(String dependencyName) {
        statically(dependencyName, null)
    }

    void statically(Closure closure) {
        statically("", closure)
    }

    void statically(String dependencyName, Closure closure) {
        Statically statically = new Statically(this.project)
        statically.dependency = dependencyName
        if (closure != null) {
            project.configure(statically, closure)
        }
        staticList.add(statically)
        storage.add(statically)
    }

    static class Statically {

        Project project
        String dependency
        List<String> libs = []
        StaticWithArgs withArgs

        Statically(Project project) {
            this.project = project
        }

        StaticWithArgs withArgs(Closure closure) {
            withArgs = new StaticWithArgs()
            project.configure(withArgs, closure)
            return withArgs
        }

        @Override
        String toString() {
            return "Statically{" +
                   "project=" + project +
                   ", dependency='" + dependency + '\'' +
                   ", libs=" + libs +
                   ", withArgs=" + withArgs +
                   '}'
        }
    }

    static class StaticWithArgs {

        String before
        String after

        StaticWithArgs() {}

        @Override
        String toString() {
            return "StaticWithArgs{" +
                   "before='" + before + '\'' +
                   ", after='" + after + '\'' +
                   '}'
        }
    }
}
