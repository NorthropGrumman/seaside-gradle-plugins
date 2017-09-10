package com.ngc.seaside.gradle.plugins.application

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin implements Plugin<Project> {

    final Instantiator instantiator;

    @Inject
    SeasideApplicationPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project p) {
        p.configure(p) {

            plugins.apply 'java'
            plugins.apply 'application'

            extensions.create("seasideApplication", SeasideApplicationExtension, instantiator, p)

            // Allow user to configure the distribution name
            afterEvaluate {
                // Make sure the user sets the mainClassName
                if (seasideApplication.mainClassName != null) {
                    project.tasks.getByName('startScripts') {
                        mainClassName = seasideApplication.mainClassName
                    }
                }

                if (seasideApplication.distributionName != null) {
                    project.tasks.getByName('distTar') {
                        compression = Compression.GZIP
                        archiveName = "${seasideApplication.distributionName}.tar.gz"
                    }
                    project.tasks.getByName('distZip') {
                        archiveName = "${seasideApplication.distributionName}.zip"
                    }
                    project.tasks.getByName('installDist') {
                        if (seasideApplication.installationDir != null) {
                            destinationDir = file(String.valueOf(seasideApplication.installationDir))
                        }
                    }
                }
            }

            /**
             * Copies files specified in includeDistributionDirs variable
             */
            task('copyApplicationResources') {
                doLast {
                    List includeDistributionDirs = seasideApplication.includeDistributionDirs
                    if (includeDistributionDirs != null) {
                        includeDistributionDirs.each {
                            applicationDistribution.from(it) {
                                into "resources"
                            }
                        }
                    } else { // Default
                        applicationDistribution.from("src/main/resources/") {
                            into "resources"
                        }
                    }
                }
            }

            /**
             * Modify installDist task to include resources and allow user to configure installation directory
             */
            installDist {
                dependsOn copyApplicationResources
            }
            // Perform installDist each build
            assemble.finalizedBy(installDist)

            /**
             * Modify distZip task to include resources
             */
            distZip {
                dependsOn copyApplicationResources
            }

            /**
             * Modify distTar task to include resources
             */
            distTar {
                dependsOn copyApplicationResources
            }

            /**
             * Modify start scripts task to allow custom start scripts
             */
            startScripts {
                doLast {
                    // Configure how APP_HOME variable is created using user command
                    if (seasideApplication.windows.appHomeCmd != null) {
                        String WINDOWS_APP_HOME_SCRIPT = "for %%? in (\"${seasideApplication.windows.appHomeCmd}\") do set APP_HOME=%%~f?"
                        windowsScript.text = windowsScript.text.replaceFirst(/set APP_HOME=.*/, WINDOWS_APP_HOME_SCRIPT)
                    }

                    // Configure how APP_HOME variable is created using user command
                    if (seasideApplication.unix.appHomeCmd != null) {
                        String UNIX_APP_HOME_SCRIPT = "\"`${seasideApplication.unix.appHomeCmd}`\""
                        unixScript.text = unixScript.text.
                                replaceFirst('(?<=APP_HOME=)((\'|\")(.*)(\'|"))(?=\n)', UNIX_APP_HOME_SCRIPT)
                    }

                    // Add system properties set by user
                    if (seasideApplication.appSystemProperties != null) {
                        seasideApplication.appSystemProperties.each { key, value ->
                            String systemProp = "\"-D" + key + "=" + value + "\""
                            p.getLogger().info("Adding " + systemProp + " to DEFAULT_JVM_OPTS")

                            // Adds system property to start scripts
                            unixScript.text = unixScript.text.
                                    replaceFirst('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                                 '\'$3 ' + systemProp + ' \'')

                            windowsScript.text = windowsScript.text.replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)',
                                                                                 '$1 ' + systemProp + ' ')
                        }
                    } else {
                        p.getLogger().debug("seasideApplication.appSystemProperties is not set.")
                    }

                    // Configure appHomeVarName to point to the APP_HOME
                    if (seasideApplication.appHomeVarName != null) {
                        p.getLogger().info("Setting " + seasideApplication.appHomeVarName + " to APP_HOME_VAR")
                        String appNameProp = "\"-D" + seasideApplication.appHomeVarName + "=APP_HOME_VAR\""

                        // Provide the app home directory has a system property.
                        unixScript.text = unixScript.text.
                                replaceFirst('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                             '\'$3 ' + appNameProp + ' \'')

                        windowsScript.text = windowsScript.text.replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)',
                                                                             '$1 ' + appNameProp + ' ')

                        windowsScript.text = windowsScript.text.replaceAll('APP_HOME_VAR', '%APP_HOME%')
                        unixScript.text = unixScript.text.replaceAll('APP_HOME_VAR', '\\$APP_HOME')
                    } else {
                        p.getLogger().debug("seasideApplication.appHomeVarName is not set.")
                    }

                    // Override generated start script with custom windows start script
                    if (seasideApplication.windows.startScript != null) {
                        p.getLogger().
                                info("Overriding Windows start script with " + seasideApplication.unix.startScript)
                        def windowsCustomScript = new File(p.getProjectDir().path,
                                                           String.valueOf(seasideApplication.windows.startScript))
                        if (windowsCustomScript.exists()) {
                            windowsScript.text = windowsCustomScript.readLines().join('\r\n')
                        }
                    }

                    // Override generated start script with custom unix start script
                    if (seasideApplication.unix.startScript != null) {
                        p.getLogger().info("Overriding Unix start script with " + seasideApplication.unix.startScript)
                        def unixCustomScript = new File(p.getProjectDir().path,
                                                        String.valueOf(seasideApplication.unix.startScript))
                        if (unixCustomScript.exists()) {
                            unixScript.text = unixCustomScript.readLines().join('\n')
                        }
                    }
                }
            }
            defaultTasks = ['build']
        }
    }
}