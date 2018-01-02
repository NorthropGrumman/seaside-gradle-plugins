package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.distribution.SeasideServiceDistributionExtension
import com.ngc.seaside.gradle.util.GradleUtil
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
 *    buildscript {*         repositories {*              mavenLocal()
 *
 *              maven {*                  url nexusConsolidated
 *}*}*
 *        dependencies {*             classpath 'com.ngc.seaside:seaside.distribution:1.1-SNAPSHOT'
 *}*}*
 *      apply plugin: 'com.ngc.seaside.service-distribution'
 * </pre>
 */
class SeasideServiceDistributionPlugin extends AbstractProjectPlugin {

    private SeasideServiceDistributionExtension distributionExtension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            applyPlugins(project)

            // Make sure that all required properties are set.
            doRequireDistributionGradleProperties(project, 'nexusConsolidated',
                                                  'nexusReleases',
                                                  'nexusSnapshots',
                                                  'nexusUsername',
                                                  'nexusPassword')

            distributionExtension = project.extensions.
                    create("seasideDistribution", SeasideServiceDistributionExtension)

            configureConfigurations(project)
            configureAfterEvaluate(project)
            createTasks(project)

            // Configure the maven related tasks here because we can't move it into a closure
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

            project.afterEvaluate {
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                artifacts {
                    archives taskResolver.findTask("tar")
                    archives taskResolver.findTask("zip")
                }
            }
        }
    }

    static void doRequireDistributionGradleProperties(Project project, String propertyName,
                                                      String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
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

        project.task('copyResources', type: Copy, dependsOn: [taskResolver.findTask('copyConfig')]) {

            from 'src/main/resources'
            exclude '**/config.ini'
            into { distributionExtension.distributionDir }
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
                    def artifact = artifacts.find { it.file
                                                            .name == name }
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
        project.logger.info(String.format("Applying plugins for %s", project.name))
        project.getPlugins().apply('java')
        project.getPlugins().apply('maven')
    }

}
