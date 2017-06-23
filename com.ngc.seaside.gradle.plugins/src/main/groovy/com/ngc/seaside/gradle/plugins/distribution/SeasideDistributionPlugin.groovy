package com.ngc.seaside.gradle.plugins.distribution

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * The seaside distribution plugin provides calls to common task, sets up the default dependencies for BLoCS and OSGi along
 * with providing nexus repository deployment settings.
 *
 * The following properties are required in your ~/.gradle/gradle.properties file to use this plugin.
 * <pre>
 *     nexusUsername     : the username to use when uploading artifacts to nexus
 *     nexusPassword     : the password to use when uploading artifacts to nexus
 *     nexusReleases     : url to the releases repository
 *     nexusSnapshots    : url to the snapshots repository
 *     nexusConsolidated : url to the maven public download site
 *                         usually a proxy to maven central and the releases and snapshots
 *     systemProp.sonar.host.url : url to the Sonarqube server
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
 *             classpath 'com.ngc.seaside:seaside.distribution:1.0'
 *             classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.5'
 *        }
 *     }
 *
 *     subprojects {
 *
 *        apply plugin: 'seaside.distribution'
 *
 *        group = 'com.ngc.seaside'
 *        version = '1.0-SNAPSHOT'
 *     }
 * </pre>
 */
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

         task('clean') {
            doLast {
               p.getLogger().trace("Removing build distribution directory '${seasideDistribution.buildDir}'.")
               delete(seasideDistribution.buildDir)
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
            into { seasideDistribution.distributionDir }
         }

         task('copyPlatformBundles', type: Copy) {
            from configurations.platform
            into { "${seasideDistribution.distributionDir}/platform" }
         }

         task('tar', type: Tar) {
            from { "${seasideDistribution.distributionDir}"}
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

         task('build', dependsOn: [copyResources,
                                   copyPlatformBundles,
                                   copyThirdPartyBundles,
                                   copyBlocsBundles,
                                   copyBundles,
                                   tar]) {
         }

         afterEvaluate {
            project.tasks.getByName('tar') { tar ->
               archiveName = "${seasideDistribution.distributionName}.tar.gz"
               destinationDir = file("${seasideDistribution.distributionDestDir}")

            }

            repositories {
               mavenLocal()

               maven {
                  url nexusConsolidated
               }
            }

         }
         defaultTasks = ['build']
      }

   }


}
