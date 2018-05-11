package com.ngc.seaside.gradle.plugins.eclipse.updatesite

import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
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
    private static final String TEST_CACHES = "caches/eclipse"
    private static final String TEST_CACHE_DIRECTORY_NAME = "$TEST_GRADLE_USER_HOME/$TEST_CACHES"
    private static final String TEST_LINUX_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-linux-gtk-x86_64"
    private static final String TEST_WINDOWS_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-win32-x86_64"
    private static final String TEST_LINUX_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/${TEST_LINUX_ECLIPSE_VERSION}.zip"
    private static final String TEST_WINDOWS_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/${TEST_WINDOWS_ECLIPSE_VERSION}.zip"

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
        extension.linuxEclipseVersion = TEST_LINUX_ECLIPSE_VERSION
        extension.windowsEclipseVersion = TEST_WINDOWS_ECLIPSE_VERSION
        extension.linuxDownloadUrl = TEST_LINUX_ECLIPSE_DOWNLOAD_URL
        extension.windowsDownloadUrl = TEST_WINDOWS_ECLIPSE_DOWNLOAD_URL
    }

    @Test
    void hasArchiveNameProperty() {
        Assert.assertNotNull("updateSiteArchiveName property doesn't exist!", extension.updateSiteArchiveName)
        Assert.assertEquals("default archive name is incorrect!", extension.updateSiteArchiveName, TEST_ARCHIVE_NAME)
    }

    @Test
    void hasCacheDirectoryProperty() {
        Assert.assertNotNull("cacheDirectory property doesn't exist!", extension.cacheDirectory)
        TestingUtilities.assertFilePathsSame(
              "default cache directory is incorrect!",
              TEST_CACHE_DIRECTORY_NAME,
              extension.cacheDirectory
        )
    }

    @Test
    void hasRequiredPropertiesWithoutDefaultValues() {
        extension = new SeasideEclipseUpdateSiteExtension(project)
        Assert.assertNull("linuxDownloadUrl property has default value!", extension.linuxDownloadUrl)
        Assert.assertNull("linuxEclipseVersion property has default value!", extension.linuxEclipseVersion)
        Assert.assertNull("windowsDownloadUrl property has default value!", extension.windowsDownloadUrl)
        Assert.assertNull("windowsEclipseVersion property has default value!", extension.windowsEclipseVersion)
        Assert.assertNull(
              "eclipsePluginsDirectory property has default value in extension!", extension.eclipsePluginsDirectory)
    }
}
