package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.SharedBuildConfiguration
import com.ngc.seaside.gradle.tasks.cpp.dependencies.StaticBuildConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 */
class SeasideCppParentPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        project.extensions.create("building", BuildingExtension, project)
        project.task('displayBuilding') << {
            println "\tHeaders: $project.building.headers"


            Collection<String> configuredStaticDeps = project.building.storage.getStaticDependencies()
            println "\tStatic $configuredStaticDeps"
            for(String dep : configuredStaticDeps) {
                Collection< StaticBuildConfiguration> configurations = project.building.storage.getStaticBuildConfigurations(dep)
                for(StaticBuildConfiguration config : configurations) {
                    println "\t  $config"
                }
            }

            Collection<String> configuredSharedDeps = project.building.storage.getSharedDependencies()
            println "\tShared $configuredSharedDeps"
            for(String dep : configuredSharedDeps) {
                Collection<SharedBuildConfiguration> configurations = project.building.storage.getSharedBuildConfigurations(dep)
                for(SharedBuildConfiguration config : configurations) {
                    println "\t $config"
                }
            }
        }
    }
}
