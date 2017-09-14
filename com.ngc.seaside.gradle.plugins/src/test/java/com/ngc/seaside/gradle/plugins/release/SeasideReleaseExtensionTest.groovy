package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.tasks.release.SeasideReleaseExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideReleaseExtensionTest {

    private SeasideReleaseExtension extension

    @Before
    void before() {
        Project project = ProjectBuilder.builder().build()
        project.version = '1.2.3.DEV'
        extension = new SeasideReleaseExtension(project)
        extension.setVersionSuffix('.DEV')
    }

    @Test
    void doesGetTagName() {
        Assert.assertEquals('v1.2.3', extension.tagName)
    }

    @Test
    void doesGetTagNameWithPrefix() {
        extension.setTagPrefix('myPrefix_')
        Assert.assertEquals('myPrefix_1.2.3', extension.tagName)
    }

    @Test
    void doesGetVersionFileName() {
        Assert.assertNotNull(extension.versionFile)
        Assert.assertEquals('build.gradle', extension.versionFile.getName())
    }

    @Test
    void doesGetSemanticVersionFromString() {
        Assert.assertEquals('1.0.0-SNAPSHOT', extension.getSemanticVersion("version = 1.0.0-SNAPSHOT"))
        Assert.assertEquals('1.0.3.RC', extension.getSemanticVersion("version = 1.0.3.RC"))
        Assert.assertEquals('1.2.0-SNAPSHOT', extension.getSemanticVersion("version =   1.2.0-SNAPSHOT"))
        Assert.assertEquals('1.2.7-SNAPSHOT', extension.getSemanticVersion("  version= 1.2.7-SNAPSHOT"))
        Assert.assertEquals('1.5.7-SNAPSHOT', extension.getSemanticVersion("version=1.5.7-SNAPSHOT"))
        Assert.assertEquals('1.5.7', extension.getSemanticVersion("version=1.5.7"))
        Assert.assertEquals(null, extension.getSemanticVersion("com.foo.bar:foo.bar:1.0.0-SNAPSHOT"))
    }
}