package com.ngc.seaside.gradle.plugins.parent

import com.ngc.seaside.gradle.plugins.distribution.SeasideDistributionPlugin
import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin
import com.ngc.seaside.gradle.plugins.util.GradleUtil
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideParentPluginIT {

    private static final String TEST_VERSION_NUMBER = " 1.2.3-SNAPSHOT     " // with whitespace

    private File projectDir
    private Project project
    private static boolean didRequireDistributionGradleProperties
    private static boolean didRequiredSystemProperties
    private static boolean didApplyPlugins
    private static boolean didCreateTask
    private SeasideParentPlugin plugin

    @Before
    void before() {

        File source = Paths.get("src/test/resources/parent/test-gradle-parent").toFile()
        projectDir = Files.createDirectories(Paths.get("build/test-gradle/test-gradle-parent")).toFile()
        FileUtils.copyDirectory(source, projectDir)

        def versionFile = new File(projectDir, 'build.gradle')

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        plugin = new SeasideParentPlugin() {


            @Override
            protected void doRequiredGradleProperties(Project project, String propertyName,
                                                                 String... propertyNames) {
                didRequireDistributionGradleProperties = true
            }

            @Override
            protected void doRequiredSystemProperties(Project project){
                didRequiredSystemProperties = true
            }

            @Override
            protected void applyPlugins(Project project){
                didApplyPlugins = true
            }

            @Override
            protected void createTasks(Project project){
                didCreateTask = true
            }

        }

        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        Assert.assertEquals("Did not require gradle properties", true,
                didRequireDistributionGradleProperties)
        Assert.assertEquals("Did not require system properties", true,
                didRequiredSystemProperties)
        Assert.assertEquals("Did not apply all plugins", true,
                didApplyPlugins)
        Assert.assertEquals("Did not apply Tasks", true,
                didCreateTask)
    }
}
