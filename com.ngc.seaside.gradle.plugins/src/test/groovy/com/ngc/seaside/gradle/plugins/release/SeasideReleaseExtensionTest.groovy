package com.ngc.seaside.gradle.plugins.release

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SeasideReleaseExtensionTest {

    private SeasideReleaseExtension extension
    private Project project = Mockito.mock(Project)
    private Project rootProject = Mockito.mock(Project)
    private Logger logger = Mockito.mock(Logger)
    private File file = Mockito.mock(File)


    @Before
    void before() {
        Mockito.when(project.version).thenReturn("1.2.3.DEV")
        Mockito.when(project.rootProject).thenReturn(rootProject)
        Mockito.when(project.getLogger()).thenReturn(logger)
        Mockito.when(rootProject.projectDir).thenReturn(file)
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
        Assert.assertEquals('1.2.0-TEST', extension.getSemanticVersion("version =   1.2.0-TEST"))
        Assert.assertEquals('1.2.7-SNAPSHOT', extension.getSemanticVersion("  version= 1.2.7-SNAPSHOT"))
        Assert.assertEquals('1.5.7-SNAPSHOT', extension.getSemanticVersion("version=1.5.7-SNAPSHOT"))

    }

    @Test(expected = GradleException.class)
    void doesThrowExceptionWhenNoVersionSuffix() {
        Mockito.doNothing().when(logger).error(Mockito.any(String))

        extension.getSemanticVersion("version=1.5.7")
        extension.getSemanticVersion("com.foo.bar:foo.bar:1.0.0-SNAPSHOT")
    }
}