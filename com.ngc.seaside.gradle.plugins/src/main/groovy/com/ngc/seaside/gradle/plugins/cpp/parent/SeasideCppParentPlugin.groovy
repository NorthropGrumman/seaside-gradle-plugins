package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.FamilyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jprovence on 9/27/2017.
 */
class SeasideCppParentPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        project.extensions.create("family", FamilyExtension, project)
        project.family.extensions.create("children", FamilyExtension.Children, project)

        project.task('displayFamily') << {
            println "\tFather: $project.family.father"
            println "\tMother: $project.family.mother"
            println "\tChildren: $project.family.children"
        }

        project.extensions.create("building", BuildingExtension, project)
        project.task('displayBuilding') << {
            println "\tHeaders: $project.building.headers"
            List< BuildingExtension.Statically> list = project.building.staticList
            for (BuildingExtension.Statically staticLib : list ) {
                println "\t$staticLib"
            }

            Collection<String> deps = project.building.storage.getStaticDependencies()
            println "\tAll Statically Configured Dependencies: $deps"

        }
    }
}
