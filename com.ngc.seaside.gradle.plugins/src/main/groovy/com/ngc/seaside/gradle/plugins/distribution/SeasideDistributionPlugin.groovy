package com.ngc.seaside.gradle.plugins.distribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.impldep.org.apache.ivy.core.module.descriptor.OverrideDependencyDescriptorMediator;

/**
 * Created by J55690 on 6/19/2017.
 */
public class SeasideDistributionPlugin implements Plugin<Project> {

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
         task('zip', type: Zip) {
            from { "${seasideDistribution.distributionDir}" }
         }

         task('build', dependsOn: [/**copyResources,
                                   copyPlatformBundles,
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
      }

   }


}
