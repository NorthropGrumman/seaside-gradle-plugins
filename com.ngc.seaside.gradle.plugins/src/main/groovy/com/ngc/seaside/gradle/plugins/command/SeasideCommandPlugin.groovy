package com.ngc.seaside.gradle.plugins.command

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 * The command plugin provides common task for building ICommand projects most notably is the
 * template task that will zip the contents of src/main/template and add it to the archives to be used
 * in the maven repository.
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

            /**
             * Create a task for generating the template zip. This will also be uploaded to Nexus.
             */
            task('createTemplate', type: Zip, dependsOn: [classes]) {
                classifier = 'template'
                from "$projectDir/src/main/template/"
                include "*"
                include "*/**"
                archiveName "${project.group}.${project.name}-${project.version}-template.zip"
                destinationDir(file("$projectDir/build/libs"))
            }


            afterEvaluate {
                configurations {
                    commandTemplate
                }

                artifacts {
                    archives createTemplate
                    commandTemplate createTemplate
                }

                install.dependsOn createTemplate
                build.dependsOn createTemplate
            }

            defaultTasks = ['build']
        }
    }
}
