package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
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

class SeasideReleasePluginIT {

    private static final String TEST_VERSION_NUMBER = " 1.2.3-SNAPSHOT     " // with whitespace

    private File projectDir
    private Project project
    private SeasideReleasePlugin plugin

    @Before
    void before() {
        File source = Paths.get("src/integrationTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/integrationTest/resources/release/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        plugin = new SeasideReleasePlugin()
        plugin.apply(project)
        project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME).uploadArtifacts = false
    }

    @Test
    void doesApplyPlugin() {
        project.evaluate()
        TaskResolver resolver = new TaskResolver(project)
        Assert.assertEquals(TEST_VERSION_NUMBER.trim(), project.version.toString())
        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))
    }

    @Test
    void doesApplyPluginFromDifferentVersionFile() {
        project.extensions.findByName(AbstractProjectPlugin.VERSION_SETTINGS_CONVENTION_NAME).versionFile = Paths.get("src/integrationTest/resources/sealion-java-hello-world/versions.gradle").toFile()
        project.evaluate()
        TaskResolver resolver = new TaskResolver(project)
        Assert.assertEquals('1.2.4-SNAPSHOT', project.version.toString())
        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(resolver.findTask(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))
    }
}
