package com.ngc.seaside.gradle.plugins.application

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.bundling.Compression
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

/**
 * Created by J57467 on 7/18/2017.
 */
class SeasideApplicationPlugin extends AbstractProjectPlugin {

    public static final String COPY_APP_RESOURCE_TASKNAME = "copyApplicationResources"
    SeasideApplicationExtension applicationExtension
    final Instantiator instantiator

    @Inject
    SeasideApplicationPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void doApply(Project project) {
        project.configure(project) {

            applyPlugins(project)

            applicationExtension = project.extensions.create("seasideApplication",
                                                             SeasideApplicationExtension,
                                                             instantiator, project)

            // Allow user to configure the distribution name
            project.afterEvaluate {
                // Make sure the user sets the mainClassName
                if (applicationExtension.mainClassName != null) {
                    project.tasks.getByName('startScripts') {
                        mainClassName = applicationExtension.mainClassName
                    }
                }

                if (applicationExtension.distributionName != null) {
                    project.tasks.getByName('distTar') {
                        compression = Compression.GZIP
                        archiveName = "${applicationExtension.distributionName}.tar.gz"
                    }
                    project.tasks.getByName('distZip') {
                        archiveName = "${applicationExtension.distributionName}.zip"
                    }
                    project.tasks.getByName('installDist') {
                        if (applicationExtension.installationDir != null) {
                            destinationDir = new File(String.valueOf(applicationExtension.installationDir))
                        }
                    }
                }
            }

            // Capture applicationDistribution Convention from application plugin
            CopySpec applicationDistribution = applicationDistribution
            createTasks(project, applicationDistribution)

            /**
             * Modify installDist task to include resources and allow user to configure installation directory
             */
            taskResolver.findTask("installDist").dependsOn(taskResolver.findTask(COPY_APP_RESOURCE_TASKNAME))

            /**
             * Perform installDist each build
             */
            taskResolver.findTask("assemble").finalizedBy(taskResolver.findTask("installDist"))

            /**
             * Modify distZip task to include resources
             */
            taskResolver.findTask("distZip").dependsOn(taskResolver.findTask(COPY_APP_RESOURCE_TASKNAME))

            /**
             * Modify distTar task to include resources
             */
            taskResolver.findTask("distTar").dependsOn(taskResolver.findTask(COPY_APP_RESOURCE_TASKNAME))

            /**
             * Modify start scripts task to allow custom start scripts
             */
            taskResolver.findTask("startScripts") {
                doLast {
                    // Configure how APP_HOME variable is created using user command
                    if (applicationExtension.windows.appHomeCmd != null) {
                        String WINDOWS_APP_HOME_SCRIPT = "for %%? in (\"${applicationExtension.windows.appHomeCmd}\") do set APP_HOME=%%~f?"
                        windowsScript.text = windowsScript.text.replaceFirst(/set APP_HOME=.*/, WINDOWS_APP_HOME_SCRIPT)
                    }

                    // Configure how APP_HOME variable is created using user command
                    if (applicationExtension.unix.appHomeCmd != null) {
                        String UNIX_APP_HOME_SCRIPT = "\"`${applicationExtension.unix.appHomeCmd}`\""
                        unixScript.text = unixScript.text.replaceFirst('(?<=APP_HOME=)((\'|\")(.*)(\'|"))(?=\n)',
                                                                       UNIX_APP_HOME_SCRIPT)
                    }

                    // Add system properties set by user
                    if (applicationExtension.appSystemProperties != null) {
                        applicationExtension.appSystemProperties.each { key, value ->
                            String systemProp = "\"-D${key}=${value}\""
                            project.getLogger().info("Adding $systemProp to DEFAULT_JVM_OPTS")

                            // Adds system property to start scripts
                            unixScript.text = unixScript.text.
                                    replaceFirst('(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
                                                 '\'$3 ' + systemProp + ' \'')

                            windowsScript.text = windowsScript.text.
                                    replaceFirst('(?<=DEFAULT_JVM_OPTS=)(.*)(?=\r\n)', '$1 ' + systemProp + ' ')
                        }
                    } else {
                        project.getLogger().debug("seasideApplication.appSystemProperties is not set.")
                    }

                    // Configure appHomeVarName to point to the APP_HOME
                    if (applicationExtension.appHomeVarName != null) {
                        project.getLogger().info("Setting " +
                                                 applicationExtension.appHomeVarName +
                                                 " to APP_HOME_VAR")
                        String appNameProp = "\"-D" +
                                             applicationExtension.appHomeVarName +
                                             "=APP_HOME_VAR\""

                        // Provide the app home directory has a system property.
                        unixScript.text = unixScript.text.
                                replaceFirst(
                                        '(?<=DEFAULT_JVM_OPTS=)((\'|\")(.*)(\'|"))(?=\n)',
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
                    if (applicationExtension.windows.startScript != null) {
                        project.getLogger().
                                info("Overriding Windows start script with $applicationExtension.unix.startScript")

                        def windowsCustomScript = new File(project.getProjectDir().path,
                                                           String.valueOf(applicationExtension.windows.startScript))
                        if (windowsCustomScript.exists()) {
                            windowsScript.text = windowsCustomScript.readLines().join('\r\n')
                        }
                    }

                    // Override generated start script with custom unix start script
                    if (applicationExtension.unix.startScript != null) {
                        project.getLogger().
                                info("Overriding Unix start script with $applicationExtension.unix.startScript")

                        def unixCustomScript = new File(project.getProjectDir().path,
                                                        String.valueOf(applicationExtension.unix.startScript))

                        if (unixCustomScript.exists()) {
                            unixScript.text = unixCustomScript.readLines().join('\n')
                        }
                    }
                }
            }
            project.defaultTasks = ['build']
        }
    }

    /**
     * Applies additional plugins to the project the project
     * @param project
     */
    private static void applyPlugins(Project project) {
        project.logger.info(String.format("Applying plugins for %s", project.name))
        project.getPlugins().apply('java')
        project.getPlugins().apply('application')
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project, CopySpec applicationDistribution) {
        /**
         * Copies files specified in includeDistributionDirs variable
         */
        project.task(COPY_APP_RESOURCE_TASKNAME) {
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
}
