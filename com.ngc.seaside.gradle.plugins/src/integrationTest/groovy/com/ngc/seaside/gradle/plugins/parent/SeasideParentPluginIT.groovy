package com.ngc.seaside.gradle.plugins.parent

import com.ngc.seaside.gradle.plugins.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideParentPluginIT {

    private File projectDir
    private Project project
    private SeasideParentPlugin plugin
    private static boolean didRequireDistributionGradleProperties

    @Before
    void before() {

        File source = Paths.get("src/integrationTest/resources/parent/com.ngc.example.parent").toFile()
        Path targetPath = Paths.get("build/integrationTest/resources/parent/com.ngc.example.parent")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()


        plugin = new SeasideParentPlugin() {


            @Override
            protected void doRequiredGradleProperties(Project project, String propertyName,
                                                                 String... propertyNames) {
                didRequireDistributionGradleProperties = true
            }

        }

        setRequiredProjectProperties(project)
        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        TaskResolver resolver = new TaskResolver(project)
        Assert.assertEquals("Did not require gradle distribution properties", true,
                didRequireDistributionGradleProperties)

        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.PARENT_SOURCE_JAR_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.PARENT_JAVADOC_JAR_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideParentPlugin.PARENT_ANALYZE_TASK_NAME))

    }

    static void setRequiredProjectProperties(Project project) {
        String test = "test"
        project.ext.nexusReleases = test
        project.ext.nexusUsername = test
        project.ext.nexusPassword = test
        project.ext.nexusSnapshots = test
        project.ext.nexusConsolidated = test
    }
}
