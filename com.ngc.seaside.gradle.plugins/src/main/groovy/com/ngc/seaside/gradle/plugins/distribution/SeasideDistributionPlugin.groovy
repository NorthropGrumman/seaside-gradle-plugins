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

         task('copyConfig', type: Copy){
            from 'src/main/resources'
            include'**/config.ini'
            expand(p.properties)
            into { seasideDistribution.distributionDir }
         }

         task('copyResources', type: Copy, dependsOn: [copyConfig]) {

            from 'src/main/resources'
            exclude'**/config.ini'

//            filter { line ->
//               line.replace('${blocs-core.version}', '${seasideDistribution.versions[\'blocs-core\']}')
//            }
            into { seasideDistribution.distributionDir }
         }

         task('copyPlatformBundles', type: Copy) {
            from configurations.platform
            into { "${seasideDistribution.distributionDir}/platform" }
         }

         task('zip', type: Zip) {
            from { "${seasideDistribution.distributionDir}" }
         }

         task('copyThirdPartyBundles', type: Copy) {
            from configurations.thirdParty
            into { "${seasideDistribution.distributionDir}/bundles" }
         }

         task('copyBlocsBundles', type: Copy) {
            from configurations.blocs {
               rename { name ->
                  def artifacts = configurations.blocs.resolvedConfiguration.resolvedArtifacts
                  def artifact = artifacts.find { it.file.name == name }
                  "${artifact.moduleVersion.id.group}.${artifact.name}-${artifact.moduleVersion.id.version}.${artifact.extension}"
               }
            }
            into { "${seasideDistribution.distributionDir}/bundles" }
         }

         task('copyBundles', type: Copy) {
            from configurations.bundles {
               rename { name ->
                  def artifacts = configurations.bundles.resolvedConfiguration.resolvedArtifacts
                  def artifact = artifacts.find { it.file.name == name }
                  "${artifact.moduleVersion.id.group}.${artifact.name}-${artifact.moduleVersion.id.version}.${artifact.extension}"
               }
            }

            into { "${seasideDistribution.distributionDir}/bundles" }
         }

         task('build', dependsOn: [copyResources,
                                   copyPlatformBundles,
                                   copyThirdPartyBundles,
                                   copyBlocsBundles,
                                   copyBundles,
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
