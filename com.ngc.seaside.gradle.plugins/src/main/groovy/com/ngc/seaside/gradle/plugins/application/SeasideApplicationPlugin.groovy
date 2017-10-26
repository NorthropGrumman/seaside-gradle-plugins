package com.ngc.seaside.gradle.plugins.application

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.application.SeasideApplicationExtension
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Compression
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin extends AbstractProjectPlugin {

    private static final String COPY_APPLICATION_RESOURCES_TASKNAME = "copyApplicationResources"
    private SeasideApplicationExtension applicationExtension
    final Instantiator instantiator;

    @Inject
    SeasideApplicationPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void doApply(Project project) {
        project.configure(project) {
            applicationExtension = project.extensions.create("seasideApplication", SeasideApplicationExtension,
                                                             instantiator, project)

            applyPlugins(project)

            // Allow user to configure the distribution name
            project.afterEvaluate {
                // Make sure the user sets the mainClassName
                if (applicationExtension.mainClassName != null) {
                    taskResolver.findTask('startScripts') {
                        mainClassName = applicationExtension.mainClassName
                    }
                }

                if (applicationExtension.distributionName != null) {
                    taskResolver.findTask('distTar') {
                        compression = Compression.GZIP
                        archiveName = "${applicationExtension.distributionName}.tar.gz"
                    }
                    taskResolver.findTask('distZip') {
                        archiveName = "${applicationExtension.distributionName}.zip"
                    }
                    taskResolver.findTask('installDist') {
                        if (applicationExtension.installationDir != null) {
                            destinationDir = new File(String.valueOf(applicationExtension.installationDir))
                        }
                    }
                }
            }

            createTasks(project)

            /**
             * Modify installDist task to include resources and allow user to configure installation directory
             */
            installDist {
                dependsOn taskResolver.findTask(COPY_APPLICATION_RESOURCES_TASKNAME)
            }
            // Perform installDist each build
            assemble.finalizedBy(installDist)

            /**
             * Modify distZip task to include resources
             */
            distZip {
                dependsOn taskResolver.findTask(COPY_APPLICATION_RESOURCES_TASKNAME)
            }

            /**
             * Modify distTar task to include resources
             */
            distTar {
                dependsOn taskResolver.findTask(COPY_APPLICATION_RESOURCES_TASKNAME)
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
                            project.getLogger().info("Adding " + systemProp + " to DEFAULT_JVM_OPTS")

                            // Adds system property to start scripts
                            unixScript.text = unixScript.text.
                                    replaceFirst('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                                 '\'$3 ' + systemProp + ' \'')

                            windowsScript.text = windowsScript.text.replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)',
                                                                                 '$1 ' + systemProp + ' ')
                        }
                    } else {
                        project.getLogger().debug("seasideApplication.appSystemProperties is not set.")
                    }

                    // Configure appHomeVarName to point to the APP_HOME
                    if (seasideApplication.appHomeVarName != null) {
                        project.getLogger().info("Setting " + seasideApplication.appHomeVarName + " to APP_HOME_VAR")
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
                        project.getLogger().debug("seasideApplication.appHomeVarName is not set.")
                    }

                    // Replace the classpath declaration with libs wildcard for Windows since the classpath was making 
                    // the command too long and Windows was balking at it.
                    windowsScript.text = windowsScript.text.replaceFirst('(set CLASSPATH=)(.*)(?=\r\n)',
                                                                         '$1' + '"%APP_HOME%\\\\lib\\\\*"')

                    // Override generated start script with custom windows start script
                    if (seasideApplication.windows.startScript != null) {
                        project.getLogger().
                                info("Overriding Windows start script with " + seasideApplication.unix.startScript)
                        def windowsCustomScript = new File(project.getProjectDir().path,
                                                           String.valueOf(seasideApplication.windows.startScript))
                        if (windowsCustomScript.exists()) {
                            windowsScript.text = windowsCustomScript.readLines().join('\r\n')
                        }
                    }

                    // Override generated start script with custom unix start script
                    if (seasideApplication.unix.startScript != null) {
                        project.getLogger().
                                info("Overriding Unix start script with " + seasideApplication.unix.startScript)
                        def unixCustomScript = new File(project.getProjectDir().path,
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

    /**
     * Create tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
        /**
         * Copies files specified in includeDistributionDirs variable
         */
        project.task('copyApplicationResources') {
            doLast {
                List includeDistributionDirs = applicationExtension.includeDistributionDirs
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
    }

    /**
     * This plugin requires the java and application plugins
     * @param project
     */
    private static void applyPlugins(Project project) {
        project.logger.info(String.format("Applying plugins for %s", project.name))
        project.getPlugins().apply('java')
        project.getPlugins().apply('application')
        project.getPlugins().apply('maven')
    }

}
