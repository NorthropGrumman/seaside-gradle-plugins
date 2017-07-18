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

         plugins.apply 'java'
         plugins.apply 'application'

         extensions.create("seasideApplication", SeasideApplicationPluginExtension)

            task('copyResources') {
                applicationDistribution.from("src/main/resources/") {
                    into "resources"
                }
            }

            /**
             * Modify distZip task to include resources
             */
            distZip {
                dependsOn copyResources
            }


            /**
             * Modify distZip task to include resources
             */
            distTar {
                dependsOn copyResources
            }



         /**
          * Modify start scripts task to allow custom start scripts
          */
         startScripts {
            doLast {

               if (seasideApplication.startScriptWindows != null) {
                  def windowsFile =  new File(p.getProjectDir().path, (String) seasideApplication.startScriptWindows)
                  if(windowsFile.exists()){
                     windowsScript.text = windowsFile.readLines().join('\r\n')
                  }
               }

               if (seasideApplication.startScriptUnix != null) {
                  def unixFile =  new File(p.getProjectDir().path, (String) seasideApplication.startScriptUnix)
                  if(unixFile.exists()) {
                     unixScript.text = unixFile.readLines().join('\n')
                  }
               }
            }
         }

         defaultTasks = ['build']
      }
   }
}