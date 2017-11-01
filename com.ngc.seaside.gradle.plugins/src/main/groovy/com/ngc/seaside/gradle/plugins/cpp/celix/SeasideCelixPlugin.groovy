package com.ngc.seaside.gradle.plugins.cpp.celix

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.cpp.parent.SeasideCppParentPlugin
import com.ngc.seaside.gradle.util.Versions
import com.ngc.seaside.gradle.tasks.cpp.celix.CelixManifestTask
import org.gradle.api.Project

/**
 * A plugin that can be applied to C++ Celix bundle projects.  This plugin will also apply the
 * {@code com.ngc.seaside.cpp.parent} plugin.
 */
class SeasideCelixPlugin extends AbstractProjectPlugin {

    public final static String MANIFEST_TASK_NAME = "manifest"
    public final static String CELIX_TASK_GROUP_NAME = "Celix"

    @Override
    void doApply(Project p) {
        p.configure(p) {
            p.getPlugins().apply('com.ngc.seaside.cpp.parent')

            addManifestTask(p)

            p.afterEvaluate {
                taskResolver.findTask(MANIFEST_TASK_NAME).file =
                      "${p.distsDir}/${p.group}.${p.name}-${p.version}/META-INF/MANIFEST.MF"
                taskResolver.findTask(SeasideCppParentPlugin.CREATE_DISTRIBUTION_ZIP_TASK_NAME)
                      .dependsOn([MANIFEST_TASK_NAME])
            }
        }
    }

    private void addManifestTask(Project p) {
        p.task(MANIFEST_TASK_NAME,
               group: CELIX_TASK_GROUP_NAME,
               type: CelixManifestTask,
               description: "Generates a MANIFEST for a Celix bundle.") {
            entry 'Bundle-SymbolicName', "${p.group}.${p.name}"
            entry 'Bundle-Name', "${p.group}.${p.name}"
            entry 'Bundle-Version', Versions.makeOsgiCompliantVersion("${p.version}")
            entry 'Bundle-Activator', "lib/linux_x86_64/lib${p.name}.so"
            entry 'Private-Library', "lib/linux_x86_64/lib${p.name}.so"
        }
    }
}
