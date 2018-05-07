package com.ngc.seaside.gradle.plugins.distribution

import com.ngc.seaside.gradle.util.TaskResolver
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SeasideServiceDistributionPluginIT {


    private File projectDir
    private Project project
    private SeasideServiceDistributionPlugin plugin

    @Before
    void before() {
        project = ProjectBuilder.builder().build()
        plugin = new SeasideServiceDistributionPlugin()
        plugin.apply(project)
    }

    @Test
    void doesApplyPlugin() {
        TaskResolver resolver = new TaskResolver(project)

        Assert.assertNotNull(resolver.findTask("copyResources"))
        Assert.assertNotNull(resolver.findTask("copyPlatformBundles"))
        Assert.assertNotNull(resolver.findTask("copyThirdPartyBundles"))
        Assert.assertNotNull(resolver.findTask("copyBundles"))
        Assert.assertNotNull(resolver.findTask("tar"))
        Assert.assertNotNull(resolver.findTask("zip"))
        Assert.assertNotNull(resolver.findTask("buildDist"))
    }

}
