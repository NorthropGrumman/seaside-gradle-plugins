package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.extensions.cpp.celix.CelixDistributionExtension
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class CelixDistributionPlugin implements Plugin<Project> {

    public static final String BUILD_TASK_NAME = "build"
    public static final String ASSEMBLE_TASK_NAME = "assemble"
    public static final String COPY_CELIX_TASK_NAME = "copyCelix"
    public static final String UNPACK_CELIX_TASK_NAME = "unpackCelix"
    public static final String COPY_BUNDLES_TASK_NAME = "copyBundles"
    public static final String CREATE_RUN_SCRIPT_TASK_NAME = "createRunScript"
    public static final String CREATE_DISTRIBUTION_ZIP_TASK_NAME = "createDistributionZip"
    public static final String DISTRIBUTION_TASK_GROUP = "Celix Distribution"
    public static final String EXTENSION_NAME = "celixDistribution"

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

            extension = p.extensions.create(EXTENSION_NAME, CelixDistributionExtension)

            applyPlugins(p)
            createDefaultProperties(p)
            createConfigurations(p)
            createTasks(p)

            afterEvaluate {
                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }
            }
        }
    }

    protected void doRequiredGradleProperties(Project project, String propertyName, String... propertyNames) {
        GradleUtil.requireProperties(project.properties, propertyName, propertyNames)
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

    }
}
