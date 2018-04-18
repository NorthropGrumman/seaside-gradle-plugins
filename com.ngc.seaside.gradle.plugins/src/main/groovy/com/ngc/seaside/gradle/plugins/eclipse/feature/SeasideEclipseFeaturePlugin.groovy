package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.feature.SeasideEclipseExtension
import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class SeasideEclipseFeaturePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_EXTENSION_NAME = "eclipseFeature"
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_CREATE_JAR_TASK_NAME = "createJar"
    public static final String ECLIPSE_COPY_FEATURE_FILE_TASK_NAME = "copyFeatureFile"

    private SeasideEclipseExtension extension

    String archiveName

    @Override
    void doApply(Project project) {
        project.configure(project) {
            extension = project.extensions.create(ECLIPSE_EXTENSION_NAME, SeasideEclipseExtension, project)
            setExtensionProperties()

            project.configurations {
                feature
            }

            createTasks(project)

            project.artifacts {
                feature project.tasks.getByName(ECLIPSE_CREATE_JAR_TASK_NAME)
            }

            defaultTasks = ["build"]
        }
    }

    private void setExtensionProperties() {
        extension.archiveName = archiveName ?: extension.archiveName
    }

    private static void createTasks(Project project) {
        def task = project.task(
              ECLIPSE_CREATE_JAR_TASK_NAME,
              type: Zip,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Create the eclipse feature file jar",
              dependsOn: ECLIPSE_COPY_FEATURE_FILE_TASK_NAME) {
            project.afterEvaluate {
                from "${project.buildDir}/tmp"
                destinationDir = project.file(project.buildDir)
                archiveName = project.extensions.getByType(SeasideEclipseExtension.class).archiveName
            }
        }

        project.task(
              ECLIPSE_COPY_FEATURE_FILE_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Copy the feature file to the correct location") {
            from 'src/main/resources', { include('feature.xml') }
            expand('osgiVersion': Versions.makeOsgiCompliantVersion("${project.version}"))
            destinationDir = project.file("${project.buildDir}/tmp")
        }

        project.task("clean") {
            project.delete(project.buildDir)
        }

        project.task("build").dependsOn(task)
    }
}
