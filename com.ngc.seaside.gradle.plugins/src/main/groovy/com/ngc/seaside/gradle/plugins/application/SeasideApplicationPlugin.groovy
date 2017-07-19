package com.ngc.seaside.gradle.plugins.application

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin implements Plugin<Project> {

    @Override
    void apply(Project p) {
        p.configure(p) {

            plugins.apply 'java'
            plugins.apply 'application'

            extensions.create("seasideApplication", SeasideApplicationPluginExtension)

            /**
             * Copies files specified in includeDistributionDirs variable
             */
            task('copyResources') {
                doLast {
                    List includeDistributionDirs = seasideApplication.includeDistributionDirs
                    if (includeDistributionDirs != null) {
                        includeDistributionDirs.each {
                            applicationDistribution.from(it) {
                                into "resources"
                            }
                        }
                    }
                }
            }

            /**
             * Modify distZip task to include resources
             */
            distZip {
                dependsOn copyResources
            }

            /**
             * Modify distZip task to include resources
             */
            distTar {
                dependsOn copyResources
            }

            /**
             * Modify start scripts task to allow custom start scripts
             */
            startScripts {

                doLast {

                    // Configure appHomeVarName to point to the APP_HOME
                    if (seasideApplication.appHomeVarName != null) {
                        p.getLogger().info("Setting " + seasideApplication.appHomeVarName + " to APP_HOME")
                        String appNameProp = "\"-D" + seasideApplication.appHomeVarName + "=APP_HOME_VAR\""

                        // Provide the app home directory has a system property.
                        unixScript.text = unixScript.text.
                                replaceAll('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                           '\'$3 ' + appNameProp + ' \'')

                        windowsScript.text = windowsScript.text.replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)',
                                                                             '$1 ' + appNameProp + ' ')

                        windowsScript.text = windowsScript.text.replaceFirst('APP_HOME_VAR', '%APP_HOME%')
                        unixScript.text = unixScript.text.replaceFirst('APP_HOME_VAR', '\\$APP_HOME')
                    } else {
                        p.getLogger().debug("seasideApplication.appHomeVarName is not set.")
                    }

                    // Add system properties set by user
                    if (seasideApplication.appSystemProperties != null) {
                        seasideApplication.appSystemProperties.each { key, value ->
                            String systemProp = "\"-D" + key + "=" + value + "\""
                            p.getLogger().info("Adding " + systemProp + " to DEFAULT_JVM_OPTS")

                            // Adds system property to start scripts
                            unixScript.text = unixScript.text.
                                    replaceAll('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                               '\'$3 ' + systemProp + ' \'')

                            windowsScript.text = windowsScript.text.replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)',
                                                                                 '$1 ' + systemProp + ' ')
                        }
                    } else {
                        p.getLogger().debug("seasideApplication.appSystemProperties is not set.")
                    }

                    // Override generated start script with custom windows start script
                    if (seasideApplication.startScriptWindows != null) {
                        p.getLogger().info("Overriding Windows start script with " + seasideApplication.startScriptUnix)
                        def windowsCustomScript = new File(p.getProjectDir().path,
                                                           String.valueOf(seasideApplication.startScriptWindows))
                        if (windowsCustomScript.exists()) {
                            windowsScript.text = windowsCustomScript.readLines().join('\r\n')
                        }
                    } else {
                        p.getLogger().debug("seasideApplication.startScriptWindows is not set.")
                    }

                    // Override generated start script with custom unix start script
                    if (seasideApplication.startScriptUnix != null) {
                        p.getLogger().info("Overriding Unix start script with " + seasideApplication.startScriptUnix)
                        def unixCustomScript = new File(p.getProjectDir().path,
                                                        String.valueOf(seasideApplication.startScriptUnix))
                        if (unixCustomScript.exists()) {
                            unixScript.text = unixCustomScript.readLines().join('\n')
                        }
                    } else {
                        p.getLogger().debug("seasideApplication.startScriptUnix is not set.")
                    }
                }
            }

            defaultTasks = ['build']
        }
    }
}