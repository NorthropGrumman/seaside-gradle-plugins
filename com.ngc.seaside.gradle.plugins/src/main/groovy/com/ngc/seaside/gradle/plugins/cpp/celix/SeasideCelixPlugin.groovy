package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.plugins.cpp.parent.SeasideCppParentPlugin
import com.ngc.seaside.gradle.plugins.util.Versions
import com.ngc.seaside.gradle.tasks.cpp.celix.CelixManifestTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A plugin that can be applied to C++ Celix bundle projects.  This plugin will also apply the
 * {@code com.ngc.seaside.cpp.parent} plugin.
 */
class SeasideCelixPlugin implements Plugin<Project> {

    public final static String MANIFEST_TASK_NAME = "manifest"
    public final static String CELIX_TASK_GROUP_NAME = "Celix"

    @Override
    void apply(Project p) {
        p.configure(p) {
            plugins.apply 'com.ngc.seaside.cpp.parent'

            addManifestTask(p)

            afterEvaluate {
                tasks.getByName(MANIFEST_TASK_NAME).file =
                      "${project.distsDir}/${project.group}.${project.name}-${project.version}/META-INF/MANIFEST.MF"
                tasks.getByName(SeasideCppParentPlugin.CREATE_DISTRIBUTION_ZIP_TASK_NAME)
                      .dependsOn([MANIFEST_TASK_NAME])
            }
        }
    }

    private void addManifestTask(Project p) {
        p.task(MANIFEST_TASK_NAME,
               group: CELIX_TASK_GROUP_NAME,
               type: CelixManifestTask,
               description: "Generates a MANIFEST for a Celix bundle.") {
            entry 'Bundle-SymbolicName', "${project.group}.${project.name}"
            entry 'Bundle-Name', "${project.group}.${project.name}"
            entry 'Bundle-Version', Versions.makeOsgiCompliantVersion("${project.version}")
            entry 'Bundle-Activator', "lib/linux_x86_64/lib${project.name}.so"
            entry 'Private-Library', "lib/linux_x86_64/lib${project.name}.so"
        }
    }
}
