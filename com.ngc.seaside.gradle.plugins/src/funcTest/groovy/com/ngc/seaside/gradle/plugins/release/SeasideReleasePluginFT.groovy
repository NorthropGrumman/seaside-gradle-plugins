package com.ngc.seaside.gradle.plugins.release

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Paths

class SeasideReleasePluginFT {

    private static final String TEST_VERSION_NUMBER = " 1.2.3-SNAPSHOT     " // with whitespace

    private File projectDir
    private Project project
    private SeasideReleasePlugin plugin

    @Before
    void before() {
        File source = Paths.get("src/funcTest/resources/release/sealion-java-hello-world").toFile()
        projectDir = Files.createDirectories(Paths.get("build/test-release/sealion-java-hello-world")).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        plugin = new SeasideReleasePlugin()
        plugin.apply(project)

    }

    @Test
    void doesApplyPlugin() {
        Assert.assertEquals(TEST_VERSION_NUMBER.trim(), project.version)
        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideReleasePlugin.RELEASE_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideReleasePlugin.RELEASE_MAJOR_VERSION_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(SeasideReleasePlugin.RELEASE_MINOR_VERSION_TASK_NAME))
    }

    @Test
    void doesGetSemanticVersionFromFile() {
        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        SeasideReleaseExtension extension = (SeasideReleaseExtension) project.extensions
                .findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME)
        Assert.assertEquals('1.2.3-SNAPSHOT', extension.getPreReleaseVersion())
    }

    @Test
    void doesSetSemanticVersionOnFile() {
        Assert.assertNotNull(project.extensions.findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME))
        SeasideReleaseExtension extension = (SeasideReleaseExtension) project.extensions
                .findByName(SeasideReleasePlugin.RELEASE_EXTENSION_NAME)
        extension.setVersionOnFile("1.2.4-SNAPSHOT")
        Assert.assertEquals('1.2.4-SNAPSHOT', extension.getPreReleaseVersionFromFile())
    }
}
