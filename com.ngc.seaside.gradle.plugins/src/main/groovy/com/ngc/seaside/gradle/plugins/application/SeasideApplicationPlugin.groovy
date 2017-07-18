package com.ngc.seaside.gradle.plugins.application

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin implements Plugin<Project> {

    @Override
    void apply(Project p) {
        p.configure(p) {

            /**
             * This plugin requires the java and maven plugins
             */
            plugins.apply 'java'
            plugins.apply 'application'

            /**
             * Modify distZip task to include resources
             */
            distZip {
                into(project.name) {
                    from '.'
                    include 'resources/*'
                }
            }

            /**
             * Modify distZip task to include resources
             */
            distTar {
                into(project.name) {
                    from '.'
                    include 'resources/*'
                }
            }

            /**
             * Modify start scripts task to allow custom start scripts
             */
            startScripts {

            }

            defaultTasks = ['build']
        }
    }
}