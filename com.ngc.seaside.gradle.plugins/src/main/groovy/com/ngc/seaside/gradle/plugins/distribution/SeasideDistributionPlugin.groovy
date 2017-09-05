package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.plugins.util.GradleUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip

/**
 * The seaside distribution plugin provides calls to common task, sets up the default dependencies for BLoCS and OSGi along
 * with providing nexus repository deployment settings.
 *
 * The following properties are required in your ~/.gradle/gradle.properties file to use this plugin.
 * <pre>
 *     nexusConsolidated : url to the maven public download site
 *                         usually a proxy to maven central and the releases and snapshots
 * </pre>
 *
 * To use this plugin in your gradle.build :
 * <pre>
 *    buildscript {
 *       repositories {
 *           mavenLocal()
 *
 *            maven {
 *              url nexusConsolidated
 *            }
 *        }
 *
 *        dependencies {
 *             classpath 'com.ngc.seaside:seaside.distribution:1.1-SNAPSHOT'
 *        }
 *     }
 *
 *      apply plugin: 'com.ngc.seaside.distribution'
 * </pre>
 */
class SeasideDistributionPlugin implements Plugin<Project> {

   @Override
   void apply(Project p) {

      p.configure(p) {

         plugins.apply 'maven'

         // Make sure that all required properties are set.
         GradleUtil.requireProperties(p.properties,
                                      'nexusConsolidated',
                                      'nexusReleases',
                                      'nexusSnapshots',
                                      'nexusUsername',
                                      'nexusPassword')

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
            archives
         }

         tasks.getByName('clean') {
            doLast {
               p.getLogger().trace("Removing build distribution directory '${seasideDistribution.buildDir}'.")
               delete(seasideDistribution.buildDir)
            }
         }

         task('copyConfig', type: Copy) {
            from 'src/main/resources'
            include '**/config.ini'
            expand(p.properties)
            into { seasideDistribution.distributionDir }
         }

         task('copyResources', type: Copy, dependsOn: [copyConfig]) {

            from 'src/main/resources'
            exclude '**/config.ini'
            into { seasideDistribution.distributionDir }
         }

         task('copyPlatformBundles', type: Copy) {
            from configurations.platform
            into { "${seasideDistribution.distributionDir}/platform" }
         }

         task('zip', type: Zip) {
            from { "${seasideDistribution.distributionDir}" }
         }

         task('tar', type: Tar) {
            from { "${seasideDistribution.distributionDir}" }
            compression = Compression.GZIP
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
//TODO: Add uploadArchives task
         uploadArchives {
            repositories {
               mavenDeployer {
                  // Use the main repo for full releases.
                  repository(url: nexusReleases) {
                     // Make sure that nexusUsername and nexusPassword are in your
                     // ${gradle.user.home}/gradle.properties file.
                     authentication(userName: nexusUsername, password: nexusPassword)
                  }
                  // If the version has SNAPSHOT in the name, use the snapshot repo.
                  snapshotRepository(url: nexusSnapshots) {
                     authentication(userName: nexusUsername, password: nexusPassword)
                  }
               }
            }
         }

         task('buildDist', dependsOn: [copyResources,
                                       copyPlatformBundles,
                                       copyThirdPartyBundles,
                                       copyBlocsBundles,
                                       copyBundles,
                                       tar,
                                       zip]) {
         }

         assemble.dependsOn(buildDist)

         afterEvaluate {
            project.tasks.getByName('tar') { tar ->
               archiveName = "${seasideDistribution.distributionName}.tar.gz"
               destinationDir = file("${seasideDistribution.distributionDestDir}")
            }

            project.tasks.getByName('zip') { zip ->
               archiveName = "${seasideDistribution.distributionName}.zip"
               destinationDir = file("${seasideDistribution.distributionDestDir}")
            }

            repositories {
               mavenLocal()

               maven {
                  url nexusConsolidated
               }
            }

            artifacts {
               archives tar
            }
         }

      }

   }

}
