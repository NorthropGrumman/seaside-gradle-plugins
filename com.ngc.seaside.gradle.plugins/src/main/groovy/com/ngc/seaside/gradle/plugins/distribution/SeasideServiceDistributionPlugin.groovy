package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.extensions.distribution.SeasideServiceDistributionExtension
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.plugins.util.TaskResolver
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
 *    buildscript {*       repositories {*           mavenLocal()
 *
 *            maven {*              url nexusConsolidated
 *}*}*
 *        dependencies {*             classpath 'com.ngc.seaside:seaside.distribution:1.1-SNAPSHOT'
 *}*}*
 *      apply plugin: 'com.ngc.seaside.distribution'
 * </pre>
 */
class SeasideServiceDistributionPlugin implements Plugin<Project> {

    SeasideServiceDistributionExtension distributionExtension
    private TaskResolver resolver

    @Override
    void apply(Project project) {
        this.resolver = new TaskResolver(project)
        project.configure(project) {
            project.plugins.apply('maven')

            // Make sure that all required properties are set.
            doRequireDistributionGradleProperties(project, 'nexusConsolidated',
                                                  'nexusReleases',
                                                  'nexusSnapshots',
                                                  'nexusUsername',
                                                  'nexusPassword')

            distributionExtension = project.extensions.create("seasideDistribution", SeasideServiceDistributionExtension)

            doConfigureConfigurations(project)
            doConfigureAfterEvaluate(project)
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

            afterEvaluate {
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                artifacts {
                    archives TaskResolver.findTask(project, "tar")
                    archives TaskResolver.findTask(project, "zip")
                }
            }
        }
    }

    protected void doRequireDistributionGradleProperties(Project project, String propertyName,
                                                         String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
    }

    protected doConfigureConfigurations(Project project) {
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

    protected doConfigureAfterEvaluate(Project project) {
        project.afterEvaluate {
            resolver.findTask('tar') { tar ->
                archiveName = "${seasideDistribution.distributionName}.tar.gz"
                destinationDir = file("${seasideDistribution.distributionDestDir}")
            }

            resolver.findTask('zip') { zip ->
                archiveName = "${seasideDistribution.distributionName}.zip"
                destinationDir = file("${seasideDistribution.distributionDestDir}")
            }
        }
    }

    protected void createTasks(Project project) {

        resolver.findTask('clean') {
            doLast {
                project.getLogger().
                        trace("Removing build distribution directory '${distributionExtension.buildDir}'.")

                project.delete(distributionExtension.buildDir)
            }
        }

        project.task('copyConfig', type: Copy) {
            from 'src/main/resources'
            include '**/config.ini'
            expand(project.properties)
            into { distributionExtension.distributionDir }
        }

        project.task('copyResources', type: Copy, dependsOn: [resolver.findTask('copyConfig')]) {

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
                    def artifact = artifacts.find { it.file.name == name }
                    "${artifact.moduleVersion.id.group}.${artifact.name}-${artifact.moduleVersion.id.version}.${artifact.extension}"
                }
            }
            into { "${distributionExtension.distributionDir}/bundles" }
        }

        project.task('buildDist', dependsOn: [resolver.findTask("copyResources"),
                                              resolver.findTask("copyPlatformBundles"),
                                              resolver.findTask("copyThirdPartyBundles"),
                                              resolver.findTask("copyBlocsBundles"),
                                              resolver.findTask("copyBundles"),
                                              resolver.findTask("tar"),
                                              resolver.findTask("zip")])
        resolver.findTask("assemble").dependsOn(resolver.findTask("buildDist"))
    }
}
