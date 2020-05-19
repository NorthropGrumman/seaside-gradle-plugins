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
package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.release.task.ReleaseTask
import com.ngc.seaside.gradle.plugins.version.SeasideVersionPlugin
import com.ngc.seaside.gradle.util.TaskResolver

import org.gradle.api.Project

class SeasideReleasePlugin extends AbstractProjectPlugin {
    public static final String RELEASE_TASK_GROUP_NAME = 'Release'
    public static final String RELEASE_TASK_NAME = 'release'
    public static final String RELEASE_MAJOR_VERSION_TASK_NAME = 'releaseMajorVersion'
    public static final String RELEASE_MINOR_VERSION_TASK_NAME = 'releaseMinorVersion'
    public static final String RELEASE_EXTENSION_NAME = 'seasideRelease'

    private SeasideReleaseExtension releaseExtension

    @Override
    void doApply(Project project) {
        project.plugins.apply(SeasideVersionPlugin)
        releaseExtension = project.extensions.create(RELEASE_EXTENSION_NAME, SeasideReleaseExtension)
        project.logger.info(String.format("Initializing release extensions for %s", project.name))
        createTasks(project)
    }

    /**
     * Create project tasks for this plugin
     * @param project
     */
    private void createTasks(Project project) {
        project.logger.info(String.format("Creating release tasks for %s", project.name))

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_TASK_NAME,
            'Creates a tagged non-SNAPSHOT release.',
            ReleaseType.PATCH,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of a non-SNAPSHOT release.',
            ReleaseType.PATCH,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_MINOR_VERSION_TASK_NAME,
            'Upgrades to next minor version & creates a tagged non-SNAPSHOT release.',
            ReleaseType.MINOR,
            releaseExtension)

       configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
                RELEASE_MINOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of an upgrade to next minor version and a non-SNAPSHOT release.',
            ReleaseType.MINOR,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
            RELEASE_MAJOR_VERSION_TASK_NAME,
            'Upgrades to next major version & creates a tagged non-SNAPSHOT release.',
            ReleaseType.MAJOR,
            releaseExtension)

        configureTask(project,
            ReleaseTask,
            RELEASE_TASK_GROUP_NAME,
                RELEASE_MAJOR_VERSION_TASK_NAME + ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX,
            'Performs a dry run of an upgrade to the next major version and a non-SNAPSHOT release.',
            ReleaseType.MAJOR,
            releaseExtension)


    }

    private static void configureTask(Project project,
                              Class type,
                              String group,
                              String name,
                              String description,
                              ReleaseType releaseType,
                              SeasideReleaseExtension releaseExtension) {
        boolean isDryRun = name.endsWith(ReleaseUtil.DRY_RUN_TASK_NAME_SUFFIX)
        project.afterEvaluate {
            def task = project.task(name,
                    type: type,
                    group: group,
                    description: description) {
                dryRun = isDryRun
                prepareForReleaseIfNeeded(releaseType)
                dependsOn {
                    project.rootProject.subprojects.collect { subproject ->
                        TaskResolver.findTask(subproject, "build")
                    }
                }
            }

            if (releaseExtension.uploadArtifacts && !isDryRun) {
                task.dependsOn(TaskResolver.findTask(project,"uploadArchives"))
            }
        }
    }
}
