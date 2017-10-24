package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.extensions.cpp.celix.CelixDistributionExtension
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import com.ngc.seaside.gradle.plugins.util.TaskResolver
import com.ngc.seaside.gradle.tasks.cpp.celix.CreateCelixRunScriptTask
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class CelixDistributionPlugin implements Plugin<Project> {

    public static final String BUILD_TASK_NAME = "build"
    public static final String ASSEMBLE_TASK_NAME = "assemble"
    public static final String COPY_CELIX_TASK_NAME = "copyCelix"
    public static final String UNPACK_CELIX_TASK_NAME = "unpackCelix"
    public static final String COPY_BUNDLES_TASK_NAME = "copyBundles"
    public static final String CREATE_RUN_SCRIPT_TASK_NAME = "createRunScript"
    public static final String CREATE_DISTRIBUTION_ZIP_TASK_NAME = "createDistributionZip"

    public static final String DISTRIBUTION_TASK_GROUP_NAME = "Celix Distribution"

    public static final String EXTENSION_NAME = "celixDistribution"

    public static final String CELIX_CONFIGURAION_NAME = "celix"
    public static final String BUNDLES_CONFIGURATION_NAME = "bundles"

    private CelixDistributionExtension extension

    @Override
    void apply(Project p) {
        p.configure(p) {
            // Make sure that all required properties are set.
            doRequiredGradleProperties(p,
                                       'nexusConsolidated',
                                       'nexusReleases',
                                       'nexusSnapshots',
                                       'nexusUsername',
                                       'nexusPassword')

            registerExtensions(p)
            applyPlugins(p)
            createConfigurations(p)
            createTasks(p)

            afterEvaluate {
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                postEvaluateConfigureTasks(p)
                configureTaskDependencies(p)

//                artifacts {
//                    archives TaskResolver.findTask(p, CREATE_DISTRIBUTION_ZIP_TASK_NAME)
//                }
            }
        }
    }

    protected void doRequiredGradleProperties(Project project, String propertyName, String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
    }

    protected void registerExtensions(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, CelixDistributionExtension)
    }

    protected void applyPlugins(Project project) {
        project.getPlugins().apply('maven')
    }

    protected void createConfigurations(Project project) {
        project.configurations {
            celix
            bundles
            archives
        }
    }

    protected void createTasks(Project project) {
        project.task(COPY_CELIX_TASK_NAME,
                     group: DISTRIBUTION_TASK_GROUP_NAME,
                     type: Copy,
                     description: 'Copies the Celix ZIP artifact to the build directory.') {
            from project.configurations.getByName(CELIX_CONFIGURAION_NAME)
            into { "${project.buildDir}" }
            rename { name -> "celix.zip" }
        }

        project.task(UNPACK_CELIX_TASK_NAME,
                     group: DISTRIBUTION_TASK_GROUP_NAME,
                     type: Copy,
                     description: 'Extracts the contents of the Celix ZIP.') {
            // Use the celix zip copied to the build directory.
            from { project.zipTree("${project.buildDir}/celix.zip") }
            // Unpack it to the location specified in the extension.
            into { extension.distributionDir }
            // We don't need headers to run so exclude them.
            exclude { element -> element.relativePath.segments[0] == 'include' }
            // Only include the configured bundles.
            exclude { element ->
                // Get the name without the file extension.
                String name = FilenameUtils.removeExtension(element.name)
                return !element.isDirectory() &&
                       element.relativePath.segments[0] == 'bundles' &&
                       !extension.defaultBundlesToInclude.contains(name)
            }
        }

        project.task(COPY_BUNDLES_TASK_NAME,
                     group: DISTRIBUTION_TASK_GROUP_NAME,
                     type: Copy,
                     description: 'Copies bundles to the distribution.') {
            from project.configurations.getByName(BUNDLES_CONFIGURATION_NAME)
            into { "${extension.distributionDir}/bundles" }
        }

        project.task(CREATE_RUN_SCRIPT_TASK_NAME,
                     group: DISTRIBUTION_TASK_GROUP_NAME,
                     type: CreateCelixRunScriptTask,
                     description: 'Creates a run script to run Celix.') {
        }

        project.task(CREATE_DISTRIBUTION_ZIP_TASK_NAME,
                     group: DISTRIBUTION_TASK_GROUP_NAME,
                     type: Zip,
                     description: 'Creates a ZIP of the distribution.') {
        }

    }

    protected void postEvaluateConfigureTasks(Project project) {
        TaskResolver.findTask(project, CREATE_RUN_SCRIPT_TASK_NAME).scriptFile = extension.runScript
        TaskResolver.findTask(project, CREATE_DISTRIBUTION_ZIP_TASK_NAME).from(extension.distributionDir)
    }

    protected void configureTaskDependencies(Project project) {
        def distroTask = TaskResolver.findTask(project, CREATE_DISTRIBUTION_ZIP_TASK_NAME)

        TaskResolver.findTask(project, UNPACK_CELIX_TASK_NAME).dependsOn(
              TaskResolver.findTask(project, COPY_CELIX_TASK_NAME))

        TaskResolver.findTask(project, CREATE_RUN_SCRIPT_TASK_NAME).dependsOn(
              TaskResolver.findTask(project, UNPACK_CELIX_TASK_NAME))

        distroTask.dependsOn(TaskResolver.findTask(project, CREATE_RUN_SCRIPT_TASK_NAME),
                             TaskResolver.findTask(project, COPY_BUNDLES_TASK_NAME))

        TaskResolver.findTask(project, BUILD_TASK_NAME).dependsOn(distroTask)
        TaskResolver.findTask(project, ASSEMBLE_TASK_NAME).dependsOn(distroTask)
    }

}
