/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.command

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 * The command plugin provides common task for building ICommand projects most notably is the
 * template task that will zip the contents of src/main/template and add it to the archives to be used
 * in the maven repository.
 */
class SeasideCommandPlugin extends AbstractProjectPlugin {

    @Override
    void doApply(Project project) {
        project.configure(project) {
            /**
             * This plugin requires the java and maven plugins
             */
            applyPlugins(project)
            createTasks(project)

            def templates = new File("$project.projectDir/src/main/templates/")
            if (templates.exists()) {
                templates.eachFile {
                    def templateFile = it
                    project.task("createTemplate${templateFile.name}", type: Zip,
                                 dependsOn: taskResolver.findTask("classes")) {
                        classifier = "template-${templateFile.name}"
                        from templateFile
                        include "*"
                        include "*/**"
                        archiveName "${project.group}.${project.name}-${project.version}-template-${templateFile.name}.zip"
                        destinationDir(new File("$project.projectDir/build/libs"))
                    }
                }
            }

            project.afterEvaluate {
                configurations {
                    commandTemplate
                }

                if (file("$project.projectDir/src/main/template/").exists()) {
                    artifacts {
                        archives taskResolver.findTask("createTemplate")
                        commandTemplate taskResolver.findTask("createTemplate")
                    }

                    taskResolver.findTask("install").dependsOn(taskResolver.findTask("createTemplate"))
                    taskResolver.findTask("build").dependsOn(taskResolver.findTask("createTemplate"))
                }

                if (templates.exists()) {
                    templates.eachFile {
                        def name = it.name
                        artifacts {
                            archives project["createTemplate${name}"]
                            commandTemplate project["createTemplate${name}"]
                        }

                        taskResolver.findTask("install").dependsOn project["createTemplate${name}"]
                        taskResolver.findTask("build").dependsOn project["createTemplate${name}"]
                    }
                }
            }

            project.defaultTasks = ['build']
        }
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
        project.logger.info(String.format("Creating tasks for %s", project.name))
        /**
         * Create a task for generating the template zip. This will also be uploaded to Nexus.
         */
        project.task('createTemplate', type: Zip, dependsOn: taskResolver.findTask("classes")) {
            classifier = 'template'
            from "$project.projectDir/src/main/template/"
            include "*"
            include "*/**"
            archiveName "${project.group}.${project.name}-${project.version}-template.zip"
            destinationDir(new File("$project.projectDir/build/libs"))
        }
    }

    /**
     * Applies additional plugins to the project the project
     * @param project
     */
    private static void applyPlugins(Project project) {
        project.logger.info(String.format("Applying plugins for %s", project.name))
        project.getPlugins().apply('java')
        project.getPlugins().apply('maven')
    }
}
