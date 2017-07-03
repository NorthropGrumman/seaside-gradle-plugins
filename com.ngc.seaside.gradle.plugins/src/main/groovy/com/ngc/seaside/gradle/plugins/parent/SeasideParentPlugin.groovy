package com.ngc.seaside.gradle.plugins.parent

import aQute.bnd.gradle.BundleTaskConvention
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import com.ngc.seaside.gradle.tasks.dependencies.ExportGradleCacheTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

/**
 * The seaside parent plugin provides calls to common task, sets up the default dependencies for BLoCS and OSGi along
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
 *    buildscript {*       repositories {*           mavenLocal()
 *
 *            maven {*              url nexusConsolidated
 *}*}*
 *        dependencies {*             classpath 'com.ngc.seaside:seaside.parent:1.0'
 *             classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.5'
 *}*}*
 *     subprojects {*
 *        apply plugin: 'seaside.parent'
 *
 *        group = 'com.ngc.seaside'
 *        version = '1.0-SNAPSHOT'
 *}* </pre>
 */
class SeasideParentPlugin implements Plugin<Project> {

    @Override
    void apply(Project p) {
        p.configure(p) {
            // Make sure that all required properties are set.
            GradleUtil.requireProperties(p.properties,
                                         'nexusConsolidated',
                                         'nexusReleases',
                                         'nexusSnapshots',
                                         'nexusUsername',
                                         'nexusPassword')
            GradleUtil.requireSystemProperties(p.properties,
                                               'sonar.host.url')

            /**
             * This plugin requires the java and maven plugins
             */
            plugins.apply 'java'
            plugins.apply 'maven'
            plugins.apply 'eclipse'
            plugins.apply 'org.sonarqube'
            plugins.apply 'jacoco'

            /**
             * Create a task for generating the source jar. This will also be uploaded to Nexus.
             */
            task('sourcesJar', type: Jar, dependsOn: [classes]) {
                classifier = 'sources'
                from sourceSets.main.allSource
            }

            /**
             * Create a task for generating the javadoc jar. This will also be uploaded to Nexus.
             */
            task('javadocJar', type: Jar, dependsOn: [classes, javadoc]) {
                classifier = 'javadoc'
                from javadoc.destinationDir
            }

            afterEvaluate {
                /**
                 * Ensure to add the doclint option to the javadoc task if using Java 8.
                 */
                if (JavaVersion.current().isJava8Compatible()) {
                    tasks.getByName('javadoc') { doc ->
                        options.addStringOption('Xdoclint:none', '-quiet')
                    }
                }

                /**
                 * Add the standard repositories. All of the seaside content should be downloaded from
                 * the nexus consolidated repository.
                 */
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                /**
                 *
                 */
                ext {
                    // The default name of the bundle.
                    bundleName = "$group" + '.' + "$project.name"
                }

                /**
                 * Augment the jar name to be $groupId.$project.name).
                 */
                tasks.getByName('sourcesJar') { jar ->
                    archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
                }

                tasks.getByName('javadocJar') { jar ->
                    archiveName = "${project.group}.${project.name}-${project.version}-${classifier}.jar"
                }

                /**
                 * Augment the jar to be OSGi compatible.
                 */
                tasks.getByName('jar') { jar ->
                    convention.plugins.bundle = new BundleTaskConvention(jar)

                    archiveName = "${project.group}.${project.name}-${project.version}.jar"

                    doLast {
                        buildBundle()
                    }

                    manifest {
                        attributes('Bundle-Name': "$bundleName",
                                   'Bundle-SymbolicName': "$bundleName")
                    }
                }

                /**
                 * The nexus upload configuration.
                 */
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

                /*
                 * Ensure we call the 2 new tasks for generating the javadoc and sources artifact jars.
                 */
                artifacts {
                    archives sourcesJar
                    archives javadocJar
                }

                /**
                 * Configure Sonarqube to use the Jacoco code coverage reports.
                 */
                sonarqube {
                    properties {
                        property 'sonar.jacoco.reportPaths', ["${project.buildDir}/jacoco/test.exec"]
                        property 'sonar.projectName', "${bundleName}"
                    }
                }

                /*
                 * Configure a task that runs the various analysis reports in the correct order.
                 */
                task('analyze', dependsOn: ['build', 'jacocoTestReport', 'sonarqube']) {
                }
            }

            task('exportGradleCache', type: ExportGradleCacheTask, group: 'Upload',
                 description: 'Copies all gradle downloaded dependencies to a custom local repository specified by -PcustomRepo= using maven2 layout.') {
                if (project.hasProperty("customRepo")) {
                    customRepo = project.property("customRepo")
                }
            }

            task('downloadDependencies', type: DownloadDependenciesTask, group: 'Upload',
                 description: 'Downloads all dependencies into a local directory specified by -PcustomRepo= using maven2 layout.') {
                if (project.hasProperty("customRepo")) {
                    customRepo = project.property("customRepo")
                }
            }

            defaultTasks = ['build']

        }
    }
}
