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
package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import org.gradle.api.Project

@Deprecated
class SeasideBatsPlugin extends AbstractProjectPlugin {

    public static final String BATS_EXTENSION_NAME = "seasideBats"
    public static final String BATS_TASK_GROUP_NAME = "Bats"
    public static final String EXTRACT_BATS_TASK_NAME = "extractBats"
    public static final String RUN_BATS_TASK_NAME = "runBats"

    String resultsFile
    String batsTestsDir

    @Override
    void doApply(Project project) {
        project.configure(project) {
            SeasideBatsExtension batsExtension = createTheBatsExtensionOnTheProject(project)
            batsExtension.resultsFile = resultsFile ?: batsExtension.resultsFile
            batsExtension.batsTestsDir = batsTestsDir ?: batsExtension.batsTestsDir

            buildscript {
                configurations {
                    classpath
                }
            }

            createTasks(project)

            project.afterEvaluate {
                project.dependencies {
                    compile "bats:bats:$batsExtension.BATS_VERSION"
                }
            }
        }
    }

    private static createTheBatsExtensionOnTheProject(Project p) {
        return p.extensions
                .create(BATS_EXTENSION_NAME, SeasideBatsExtension, p)
    }

    private void createTasks(Project project) {
        project.task(
                EXTRACT_BATS_TASK_NAME,
                type: ExtractBatsTask,
                group: BATS_TASK_GROUP_NAME,
                description: "Extract the bats release archive",
                dependsOn: "build")

        project.task(
                RUN_BATS_TASK_NAME,
                type: RunBatsTask,
                group: BATS_TASK_GROUP_NAME,
                description: "Run the bats command on the specified directory",
                dependsOn: EXTRACT_BATS_TASK_NAME)
    }
}
