package com.ngc.seaside.gradle.plugins.command

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 *
 */
class SeasideCommandPlugin implements Plugin<Project> {

    @Override
    void apply(Project p) {
        p.configure(p) {

            /**
             * This plugin requires the java and maven plugins
             */
            plugins.apply 'java'
            plugins.apply 'maven'
            plugins.apply 'eclipse'
            plugins.apply 'org.sonarqube'
            plugins.apply 'jacoco'

            /**
             * Create a task for generating the source jar. This will also be uploaded to Nexus.
             */
            task('template', type: Zip, dependsOn: [classes]) {
                classifier = 'template'
                from "$projectDir/src/main/template/"
                include "*"
                include "*/**"
                archiveName "${project.group}.${project.name}-${project.version}-template.zip"
                destinationDir(file("$projectDir/build/libs"))
            }


            afterEvaluate {
                artifacts {
                    archives template
                }

                build.dependsOn template
            }

            defaultTasks = ['build']
        }
    }
}
