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
package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin
import com.ngc.seaside.gradle.util.GradleUtil

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

/**
 * A plugin that can be applied to a project which will produce a ZIP that contains Celix, bundles, and configuration
 * files.  The distribution will contain any configured bundles and will have a start script which makes it a running
 * application out of the box.  This plugin is typically applied to projects that only produce a ZIP file; these
 * projects usually don't have code.
 */
class CelixDistributionPlugin extends AbstractProjectPlugin {

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
    void doApply(Project p) {
        p.configure(p) {
            registerExtensions(p)
            applyPlugins(p)
            createConfigurations(p)
            createTasks(p)

            p.afterEvaluate {
                postEvaluateConfigureTasks()
                configureTaskDependencies()

                artifacts {
                    archives taskResolver.findTask(p, CREATE_DISTRIBUTION_ZIP_TASK_NAME)
                }
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
        project.plugins.apply('maven')
        project.plugins.apply(SeasideRepositoryPlugin)
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

    protected void postEvaluateConfigureTasks() {
        taskResolver.findTask(CREATE_RUN_SCRIPT_TASK_NAME).scriptFile = extension.runScript
        taskResolver.findTask(CREATE_DISTRIBUTION_ZIP_TASK_NAME).from(extension.distributionDir)
    }

    protected void configureTaskDependencies() {
        def distroTask = taskResolver.findTask(CREATE_DISTRIBUTION_ZIP_TASK_NAME)

        taskResolver.findTask(UNPACK_CELIX_TASK_NAME).dependsOn(
                taskResolver.findTask(COPY_CELIX_TASK_NAME))

        taskResolver.findTask(CREATE_RUN_SCRIPT_TASK_NAME).dependsOn(
                taskResolver.findTask(UNPACK_CELIX_TASK_NAME))

        distroTask.dependsOn(taskResolver.findTask(CREATE_RUN_SCRIPT_TASK_NAME),
                             taskResolver.findTask(COPY_BUNDLES_TASK_NAME))

        taskResolver.findTask(BUILD_TASK_NAME).dependsOn(distroTask)
        taskResolver.findTask(ASSEMBLE_TASK_NAME).dependsOn(distroTask)
    }

}
