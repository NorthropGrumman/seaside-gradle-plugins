package com.ngc.seaside.gradle.plugins.distribution

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideDistributionPluginIT {


    private File projectDir
    private Project project
    private SeasideDistributionPlugin plugin
    private static boolean configuredUploadArchives
    private static boolean didRequireDistributionGradleProperties

    @Before
    void before() {
        File source = Paths.get("src/integTest/resources/distribution/test-gradle-distribution").toFile()
        projectDir = Files.createDirectories(Paths.get("build/test-distribution/test-gradle-distribution")).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        plugin = new SeasideDistributionPlugin() {

            @Override
            protected void doConfigureUploadArchives() {
                configuredUploadArchives = true
            }

            @Override
            protected void doRequireDistributionGradleProperties(Project project, String propertyName,
                                                                 String... propertyNames) {
                didRequireDistributionGradleProperties = true
                Assert.assertNotNull("Project properties cannot be null", properties)
            }
        }

        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        Assert.assertEquals("Did not configure upload archives", true, configuredUploadArchives)
        Assert.assertEquals("Did not require gradle distribution properties", true,
                            didRequireDistributionGradleProperties)
    }
}
