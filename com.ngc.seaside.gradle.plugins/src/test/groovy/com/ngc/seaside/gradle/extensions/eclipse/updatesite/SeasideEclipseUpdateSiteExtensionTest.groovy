package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.internal.os.OperatingSystem
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class SeasideEclipseUpdateSiteExtensionTest {
    private static final String TEST_PROJECT_VERSION = "1.2.3-SNAPSHOT"
    private static final String TEST_PROJECT_GROUP = "test.project.group"
    private static final String TEST_PROJECT_NAME = "test-project-name"
    private static final String TEST_ARCHIVE_NAME =
          "${TEST_PROJECT_GROUP}.${TEST_PROJECT_NAME}-${TEST_PROJECT_VERSION}.zip"
    private static final String TEST_GRADLE_USER_HOME = "/home/user/.gradle"
    private static final String TEST_CACHES = "eclipse"
    private static final String TEST_CACHE_DIRECTORY_NAME = "$TEST_GRADLE_USER_HOME/$TEST_CACHES"
    private static final String TEST_ECLIPSE_VERSION =
          "eclipse-dsl-oxygen-2-${SeasideEclipseUpdateSiteExtension.OS_SPECIFIER}-x86_64"
    private static final String TEST_ECLIPSE_PLUGINS_DIRECTORY_NAME =
          "$TEST_CACHE_DIRECTORY_NAME/$TEST_ECLIPSE_VERSION/plugins"

    private SeasideEclipseUpdateSiteExtension extension

    @Mock
    private Project project

    @Before
    void before() {
        when(project.group).thenReturn(TEST_PROJECT_GROUP)
        when(project.name).thenReturn(TEST_PROJECT_NAME)
        when(project.version).thenReturn(TEST_PROJECT_VERSION)

        File file = mock(File.class)
        when(file.absolutePath).thenReturn(TEST_GRADLE_USER_HOME)

        Gradle gradle = mock(Gradle.class)
        when(gradle.gradleUserHomeDir).thenReturn(file)
        when(project.gradle).thenReturn(gradle)

        extension = new SeasideEclipseUpdateSiteExtension(project)
    }

    @Test
    void hasArchiveNameProperty() {
        Assert.assertNotNull("archiveName property doesn't exist!", extension.archiveName)
        Assert.assertEquals("default archive name is incorrect!", extension.archiveName, TEST_ARCHIVE_NAME)
    }

    @Test
    void hasCacheDirectoryProperty() {
        Assert.assertNotNull("cacheDirectory property doesn't exist!", extension.cacheDirectory)
        Assert.assertEquals(
              "default cache directory is incorrect!",
              extension.cacheDirectory,
              TEST_CACHE_DIRECTORY_NAME
        )
    }

    @Test
    void hasEclipseVersionProperty() {
        Assert.assertNotNull("eclipseVersion property doesn't exist!", extension.eclipseVersion)
        Assert.assertEquals(
              "default eclipse version is incorrect!",
              extension.eclipseVersion,
              TEST_ECLIPSE_VERSION
        )
    }

    @Test
    void hasEclipsePluginsDirectoryProperty() {
        Assert.assertNotNull("eclipsePluginsDirectory property doesn't exist!", extension.eclipsePluginsDirectory)
        Assert.assertEquals(
              "default eclipse plugins directory is incorrect!",
              extension.eclipsePluginsDirectory,
              TEST_ECLIPSE_PLUGINS_DIRECTORY_NAME
        )
    }

    @Test
    void hasRequiredPropertiesWithoutDefaultValues() {
        Assert.assertNull("linuxDownloadUrl property doesn't exist!", extension.linuxDownloadUrl)
        Assert.assertNull("windowsDownloadUrl property doesn't exist!", extension.windowsDownloadUrl)
    }
}
