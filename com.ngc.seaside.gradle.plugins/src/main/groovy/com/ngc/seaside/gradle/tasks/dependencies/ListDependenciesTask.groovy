/*
 * Copyright 2015 Thorsten Ehlers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ngc.seaside.gradle.tasks.dependencies

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Gradle-Task that downloads all dependencies into a local directory based repository.
 */
class ListDependenciesTask extends DefaultTask {

    boolean showTransitive = false
    @TaskAction
    def listDependencies() {

         //Properties needs to be fixed/implemented
//        if(System.properties.hasProperty("showTransitive")){
//            showTransitive = System.properties.getProperty("showTransitive")
//        }

        project.configurations.each { configuration ->
            configuration.setTransitive(showTransitive)
        }

        listDependenciesForProject(project)

        project.subprojects.each {
            listDependenciesForProject(it)
        }
    }

    void listDependenciesForProject(Project currentProject) {
        println(currentProject.name)
        def libraryFiles = [:]
        def componentIds = [] as Set
        (currentProject.configurations + currentProject.buildscript.configurations).each { configuration ->
            println("   "+configuration)
            if (isConfigurationResolvable(configuration)) {
                componentIds.addAll(
                      configuration.incoming.resolutionResult.allDependencies.collect {
                          if (it.hasProperty('selected')) {
                              println("       " + it.selected.id)
                              return it.selected.id
                          }

                          if (it.hasProperty('attempted')) {
                              project.getLogger().warn("Unable to save artifacts of ${it.attempted.displayName}")
                          }
                      }
                )

                configuration.incoming.files.each { file ->
                    libraryFiles[file.name] = file
                }
            }
        }

        println("Dependencies of all configurations: \n ${componentIds.collect { it.toString() }.join('\n')}")
        project.getLogger().info("Dependencies of all configurations: ${componentIds.collect { it.toString() }.join(', ')}")
    }

    /**
     * Gradle 3.4 introduced the configuration 'apiElements' that isn't resolvable. So
     * we have to check before accessing it.
     */
    boolean isConfigurationResolvable(configuration) {
        if (!configuration.metaClass.respondsTo(configuration, 'isCanBeResolved')) {
            // If the recently introduced method 'isCanBeResolved' is unavailable, we
            // assume (for now) that the configuration can be resolved.
            return true
        }

        return configuration.isCanBeResolved()
    }
}
