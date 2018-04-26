package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.eclipse.feature.SeasideEclipseFeatureExtension
import com.ngc.seaside.gradle.util.Versions
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

/**
 * Plugin used for building the feature project of an Eclipse plugin. Projects that use this plugin should include
 * the file {@code feature.xml} in {@code src/main/resources}. By default, building a project with this plugin will
 * build the Eclipse feature jar.
 *
 * <p> This plugin creates the {@value #ECLIPSE_FEATURE_EXTENSION_NAME} extension name that uses
 * {@link SeasideEclipseFeatureExtension}.
 */
class SeasideEclipseFeaturePlugin extends AbstractProjectPlugin {
    public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse"

    public static final String ECLIPSE_FEATURE_EXTENSION_NAME = "eclipseFeature"
    public static final String ECLIPSE_CREATE_JAR_TASK_NAME = "createJar"
    public static final String ECLIPSE_COPY_FEATURE_FILE_TASK_NAME = "copyFeatureFile"

    private SeasideEclipseFeatureExtension extension

    String archiveName

    @Override
    void doApply(Project project) {
        project.configure(project) {
            apply plugin: 'base'
            createExtension(project)

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
                archiveName = project.extensions.getByType(SeasideEclipseFeatureExtension.class).archiveName
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
    }
}
