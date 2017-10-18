package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.plugins.util.Versions
import com.ngc.seaside.gradle.tasks.cpp.celix.CelixManifestTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin that can be applied to C++ Celix bundle projects.  This plugin will also apply the
 * {@code com.ngc.seaside.cpp.parent} plugin.
 */
class SeasideCelixPlugin implements Plugin<Project>  {

    final static String MANIFEST_TASK_NAME = "manifest"

    @Override
    void apply(Project p) {
        p.configure(p) {
            plugins.apply 'com.ngc.seaside.cpp.parent'

            task(MANIFEST_TASK_NAME, type: CelixManifestTask) {
                entry 'Bundle-SymbolicName', "${project.group}.${project.name}"
                entry 'Bundle-Name', "${project.group}.${project.name}"
                entry 'Bundle-Version', Versions.makeOsgiCompliantVersion("${project.version}")
            }

            afterEvaluate {
                tasks.getByName(MANIFEST_TASK_NAME).file = "${project.distsDir}/${project.group}.${project.name}-${project.version}/META-INF/MANIFEST.MF"
                tasks.getByName("createDistributionZip").dependsOn([MANIFEST_TASK_NAME])
            }
        }
    }
}
