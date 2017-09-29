package com.ngc.seaside.gradle.plugins.distribution

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

class SeasideDistributionPluginIT {


    private File projectDir
    private Project project
    private SeasideDistributionPlugin plugin
    private static boolean didRequireDistributionGradleProperties


    @Before
    void before() {
        File source = Paths.get("src/integrationTest/resources/distribution/com.ngc.example.distribution").toFile()
        Path targetPath = Paths.get("build/integrationTest/resources/distribution/com.ngc.example.distribution")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()


        plugin = new SeasideDistributionPlugin() {

            @Override
            protected void doRequireDistributionGradleProperties(Project project, String propertyName,
                                                                 String... propertyNames) {
                didRequireDistributionGradleProperties = true
                Assert.assertNotNull("Project properties cannot be null", properties)
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

        Assert.assertNotNull(resolver.findTask("copyResources"))
        Assert.assertNotNull(resolver.findTask("copyPlatformBundles"))
        Assert.assertNotNull(resolver.findTask("copyThirdPartyBundles"))
        Assert.assertNotNull(resolver.findTask("copyBundles"))
        Assert.assertNotNull(resolver.findTask("tar"))
        Assert.assertNotNull(resolver.findTask("zip"))
        Assert.assertNotNull(resolver.findTask("buildDist"))
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
