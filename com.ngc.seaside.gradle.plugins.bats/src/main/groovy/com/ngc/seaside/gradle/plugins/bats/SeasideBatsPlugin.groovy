/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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
