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
import com.ngc.seaside.gradle.plugins.cpp.parent.SeasideCppParentPlugin
import com.ngc.seaside.gradle.util.Versions
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
