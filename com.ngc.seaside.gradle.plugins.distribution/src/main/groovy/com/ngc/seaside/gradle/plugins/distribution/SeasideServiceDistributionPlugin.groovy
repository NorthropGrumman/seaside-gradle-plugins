/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin
import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip

/**
 * The seaside distribution plugin provides calls to common task, sets up the default dependencies for BLoCS and OSGi
 * along with providing nexus repository deployment settings.
 *
 * <p> This plugin applies the {@link SeasideRepositoryPlugin}. By default, the following properties are required in
 * your ~/.gradle/gradle.properties file to use this plugin.
 * <pre>
 *     nexusConsolidated : url to the maven public download site
 *                         usually a proxy to maven central and the releases and snapshots
 * </pre>
 *
 * To use this plugin in your gradle.build :
 * <pre>
 *    buildscript {
 *       repositories {
 *          mavenLocal()
 *
 *          maven {
 *             url nexusConsolidated
 *          }
 *       }
 *       dependencies {
 *          classpath 'com.ngc.seaside:seaside.distribution:1.1-SNAPSHOT'
 *       }
 *    }
 *    apply plugin: 'com.ngc.seaside.service-distribution'
 * </pre>
 */
class SeasideServiceDistributionPlugin extends AbstractProjectPlugin {

   /**
    * Used to set executable permissions on the eclipse executable.  Equivalent to unix file permissions: rwxr-xr-x.
    */
   private static final int UNIX_EXECUTABLE_PERMISSIONS = 493

   private SeasideServiceDistributionExtension distributionExtension

   @Override
   void doApply(Project project) {
      project.configure(project) {
         applyPlugins(project)

         distributionExtension = project.extensions.
               create("seasideDistribution", SeasideServiceDistributionExtension)

         configureConfigurations(project)
         configureAfterEvaluate(project)
         createTasks(project)

         project.afterEvaluate {
            artifacts {
               archives taskResolver.findTask("tar")
               archives taskResolver.findTask("zip")
            }
         }
      }
   }

   void configureConfigurations(Project project) {
      project.configurations {
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
   }

   void configureAfterEvaluate(Project project) {
      project.afterEvaluate {
         taskResolver.findTask('tar') { tar ->
            archiveName = "${distributionExtension.distributionName}.tar.gz"
            destinationDir = project.file("${distributionExtension.distributionDestDir}")
         }

         taskResolver.findTask('zip') { zip ->
            archiveName = "${distributionExtension.distributionName}.zip"
            destinationDir = project.file("${distributionExtension.distributionDestDir}")
         }
      }
   }

   /**
    * Create project tasks for this plugin
    * @param project
    */
   void createTasks(Project project) {

      taskResolver.findTask('clean') {
         doLast {
            project.getLogger().debug("Removing build distribution directory '${distributionExtension.buildDir}'.")
            project.delete(distributionExtension.buildDir)
         }
      }

      project.task('copyConfig', type: Copy) {
         from 'src/main/resources'
         include '**/config.ini'
         expand(project.properties)
         into { distributionExtension.distributionDir }
      }

      project.task('copyResources', type: Copy, dependsOn: [taskResolver.findTask('copyConfig')]) { CopySpec spec ->
         spec.from 'src/main/resources'
         spec.exclude '**/config.ini'
         spec.eachFile { f ->
            if (f.name.equals("start")) {
               f.mode = UNIX_EXECUTABLE_PERMISSIONS
            }
         }
         spec.into { distributionExtension.distributionDir }
      }

      project.task('copyPlatformBundles', type: Copy) {
         from project.configurations.getByName("platform")
         into { "${distributionExtension.distributionDir}/platform" }
      }

      project.task('tar', type: Tar) {
         from { "${distributionExtension.distributionDir}" }
         compression = Compression.GZIP
      }

      project.task('zip', type: Zip) {
         from { "${distributionExtension.distributionDir}" }
      }

      project.task('copyThirdPartyBundles', type: Copy) {
         from project.configurations.getByName("thirdParty")
         into { "${distributionExtension.distributionDir}/bundles" }
      }

      project.task('copyBlocsBundles', type: Copy) {
         from project.configurations.getByName("blocs") {
            rename { name ->
               def artifacts = project.configurations.blocs.resolvedConfiguration.resolvedArtifacts
               def artifact = artifacts.find { it.file.name == name }
               "${artifact.moduleVersion.id.group}.${artifact.name}-${artifact.moduleVersion.id.version}.${artifact.extension}"
            }
         }
         into { "${distributionExtension.distributionDir}/bundles" }
      }

      project.task('copyBundles', type: Copy) {
         from project.configurations.getByName("bundles") {
            rename { name ->
               def artifacts = project.configurations.bundles.resolvedConfiguration.resolvedArtifacts
               def artifact = artifacts.find { it.file.name == name }
               "${artifact.moduleVersion.id.group}.${artifact.name}-${artifact.moduleVersion.id.version}.${artifact.extension}"
            }
         }
         into { "${distributionExtension.distributionDir}/bundles" }
      }

      project.task('buildDist', dependsOn: [taskResolver.findTask("copyResources"),
                                            taskResolver.findTask("copyPlatformBundles"),
                                            taskResolver.findTask("copyThirdPartyBundles"),
                                            taskResolver.findTask("copyBlocsBundles"),
                                            taskResolver.findTask("copyBundles"),
                                            taskResolver.findTask("tar"),
                                            taskResolver.findTask("zip")])
      taskResolver.findTask("assemble").dependsOn(taskResolver.findTask("buildDist"))
   }

   /**
    * Applies additional plugins to the project the project
    * @param project
    */
   static void applyPlugins(Project project) {
      project.logger.info("Applying plugins for ${project.name}")
      project.plugins.apply('java')
      project.plugins.apply('maven')
      project.plugins.apply(SeasideRepositoryPlugin)
      project.plugins.apply(SeasideCiPlugin)
   }

}
