package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
import com.ngc.seaside.gradle.tasks.bats.ExtractBatsTask
import com.ngc.seaside.gradle.tasks.bats.RunBatsTask
import org.gradle.api.Project

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
