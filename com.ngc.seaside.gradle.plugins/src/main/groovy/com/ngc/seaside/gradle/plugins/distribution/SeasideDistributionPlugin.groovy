package com.ngc.seaside.gradle.plugins.distribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class SeasideDistributionPlugin implements Plugin<Project> {

   @Override
   void apply(Project p) {

      p.configure(p) {

         extensions.create("seasideDistribution", SeasideDistributionPluginExtension)

         configurations {
            bundles {
               transitive = false
            }
            blocs {
               transitive = false
            }
            thirdParty {
               transitive = true
            }
            platform {
               transitive = false
            }
         }

         task('copyResources', type: Copy) {
            from 'src/main/resources'
//            filter { line ->
//               line.replace('${blocs-core.version}', "${seasideDistribution.versions['blocs-core']}")
//            }
            into { seasideDistribution.distributionDir }
         }
         task('zip', type: Zip) {
            from { "${seasideDistribution.distributionDir}" }
         }

         task('build', dependsOn: [copyResources,
                                   /** copyPlatformBundles,
                                   copyThirdPartyBundles,
                                   copyBlocsBundles,
                                   copyBundles,**/
                                   zip]) {
         }

         afterEvaluate {
            project.tasks.getByName('zip') { zip ->
               archiveName = "${seasideDistribution.distributionName}.zip"
               destinationDir = file("${seasideDistribution.distributionDestDir}")

            }
         }
         defaultTasks = ['build']
      }

   }


}
