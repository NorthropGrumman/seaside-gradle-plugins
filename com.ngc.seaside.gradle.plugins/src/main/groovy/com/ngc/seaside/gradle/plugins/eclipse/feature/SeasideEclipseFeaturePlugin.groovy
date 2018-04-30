package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.feature.SeasideEclipseFeatureExtension
import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Paths

/**
 * Plugin used for building the feature project of an Eclipse plugin. Projects that use this plugin should include
 * the file {@code feature.xml} in {@code src/main/resources}. By default, building a project with this plugin will
 * build the Eclipse feature jar.
 *
 * <p> This plugin creates the {@value #ECLIPSE_FEATURE_EXTENSION_NAME} extension name that uses
 * {@link SeasideEclipseFeatureExtension}.
 */
class SeasideEclipseFeaturePlugin extends AbstractProjectPlugin {
    /**
     * The eclipse task group name.
     */
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    /**
     * The eclipse feature extension name.
     */
    public static final String ECLIPSE_FEATURE_EXTENSION_NAME = "eclipseFeature"

    /**
     * The name of the task for creating the feature jar file.
     */
    public static final String ECLIPSE_CREATE_JAR_TASK_NAME = "createJar"

    /**
     * The name of the task for copying the feature file.
     */
    public static final String ECLIPSE_COPY_FEATURE_FILE_TASK_NAME = "copyFeatureFile"

    /**
     * The name of the archive to create for the feature.
     */
    public String archiveName

    private SeasideEclipseFeatureExtension extension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            apply plugin: 'base'

            createExtension(project)

            project.afterEvaluate {
                configureTasks(project)
            }

            project.configurations {
                feature
            }

            createTasks(project)

            project.artifacts {
                feature project.tasks.getByName(ECLIPSE_CREATE_JAR_TASK_NAME)
            }
        }
    }

    private void createExtension(Project project) {
        extension = project.extensions
              .create(ECLIPSE_FEATURE_EXTENSION_NAME, SeasideEclipseFeatureExtension, project)
        setExtensionProperties()
    }

    private void setExtensionProperties() {
        extension.archiveName = archiveName ?: extension.archiveName
    }

    private void configureTasks(Project project) {
        project.getTasks().getByName(ECLIPSE_CREATE_JAR_TASK_NAME) {
            from Paths.get(project.buildDir.absolutePath, "tmp")
            destinationDir = project.file(project.buildDir)
            archiveName = this.extension.archiveName
        }
    }

    private static void createTasks(Project project) {
        project.task(
              ECLIPSE_CREATE_JAR_TASK_NAME,
              type: Zip,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Create the eclipse feature file jar",
              dependsOn: ECLIPSE_COPY_FEATURE_FILE_TASK_NAME)

        project.task(
              ECLIPSE_COPY_FEATURE_FILE_TASK_NAME,
              type: Copy,
              group: ECLIPSE_TASK_GROUP_NAME,
              description: "Copy the feature file to the correct location") {
            from Paths.get("src", "main", "resources"), { include("feature.xml") }
            expand("osgiVersion": Versions.makeOsgiCompliantVersion("${project.version}"))
            destinationDir = project.file(Paths.get(project.buildDir.absolutePath, "tmp"))
        }
    }
}
