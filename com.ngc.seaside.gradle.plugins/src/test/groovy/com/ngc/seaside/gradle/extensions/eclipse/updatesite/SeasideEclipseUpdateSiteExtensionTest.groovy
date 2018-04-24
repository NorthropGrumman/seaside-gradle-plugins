package com.ngc.seaside.gradle.extensions.eclipse.updatesite

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.junit.Assert
import org.junit.Assume
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
    private static final String TEST_LINUX_ECLIPSE_PLUGINS_DIRECTORY_NAME =
          "$TEST_CACHE_DIRECTORY_NAME/$TEST_LINUX_ECLIPSE_VERSION/plugins"
    private static final String TEST_WINDOWS_ECLIPSE_PLUGINS_DIRECTORY_NAME =
          "$TEST_CACHE_DIRECTORY_NAME/$TEST_WINDOWS_ECLIPSE_VERSION/plugins"
    private static final String TEST_LINUX_ECLIPSE_ARCHIVE_NAME =
          "$TEST_CACHE_DIRECTORY_NAME/${TEST_LINUX_ECLIPSE_VERSION}.zip"
    private static final String TEST_WINDOWS_ECLIPSE_ARCHIVE_NAME =
          "$TEST_CACHE_DIRECTORY_NAME/${TEST_WINDOWS_ECLIPSE_VERSION}.zip"

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
    void hasRequiredPropertiesWithoutDefaultValues() {
        extension = new SeasideEclipseUpdateSiteExtension(project)
        Assert.assertNull("linuxDownloadUrl property has default value!", extension.linuxDownloadUrl)
        Assert.assertNull("linuxEclipseVersion property has default value!", extension.linuxEclipseVersion)
        Assert.assertNull("windowsDownloadUrl property has default value!", extension.windowsDownloadUrl)
        Assert.assertNull("windowsEclipseVersion property has default value!", extension.windowsEclipseVersion)
    }

    @Test
    void returnsCorrectEclipseVersionOnLinux() {
        Assume.assumeFalse(
              "Current OS is Windows, skipping Linux test.",
              System.getProperty("os.name").toLowerCase().startsWith("win")
        )
        Assert.assertEquals(
              "incorrect eclipse version returned on linux",
              extension.linuxEclipseVersion,
              extension.getEclipseVersion()
        )
    }

    @Test
    void returnsCorrectEclipseVersionOnWindows() {
        Assume.assumeFalse(
              "Current OS is Linux, skipping Windows test.",
              System.getProperty("os.name").toLowerCase().startsWith("linux")
        )
        Assert.assertEquals(
              "incorrect eclipse version returned on windows",
              extension.windowsEclipseVersion,
              extension.getEclipseVersion()
        )
    }

    @Test
    void returnsCorrectEclipseDownloadUrlOnLinux() {
        Assume.assumeFalse(
              "Current OS is Windows, skipping Linux test.",
              System.getProperty("os.name").toLowerCase().startsWith("win")
        )
        Assert.assertEquals(
              "incorrect eclipse download url returned for linux",
              extension.linuxDownloadUrl,
              extension.getEclipseDownloadUrl()
        )
    }

    @Test
    void returnsCorrectEclipseDownloadUrlOnWindows() {
        Assume.assumeFalse(
              "Current OS is Linux, skipping Windows test.",
              System.getProperty("os.name").toLowerCase().startsWith("linux")
        )
        Assert.assertEquals(
              "incorrect eclipse download url returned for windows",
              extension.windowsDownloadUrl,
              extension.getEclipseDownloadUrl()
        )
    }

    @Test
    void returnsCorrectEclipsePluginsDirectoryOnLinux() {
        Assume.assumeFalse(
              "Current OS is Windows, skipping Linux test.",
              System.getProperty("os.name").toLowerCase().startsWith("win")
        )
        Assert.assertEquals(
              "default eclipse plugins directory on Linux is incorrect!",
              extension.getEclipsePluginsDirectory(),
              TEST_LINUX_ECLIPSE_PLUGINS_DIRECTORY_NAME
        )
    }

    @Test
    void returnsCorrectEclipsePluginsDirectoryOnWindows() {
        Assume.assumeFalse(
              "Current OS is Linux, skipping Windows test.",
              System.getProperty("os.name").toLowerCase().startsWith("linux")
        )
        Assert.assertEquals(
              "default eclipse plugins directory on Windows is incorrect!",
              extension.getEclipsePluginsDirectory(),
              TEST_WINDOWS_ECLIPSE_PLUGINS_DIRECTORY_NAME
        )
    }

    @Test
    void returnsCorrectEclipseArchiveNameOnLinux() {
        Assume.assumeFalse(
              "Current OS is Windows, skipping Linux test.",
              System.getProperty("os.name").toLowerCase().startsWith("win")
        )
        Assert.assertEquals(
              "default eclipse archive name on Linux is incorrect!",
              extension.getEclipseArchiveName(),
              TEST_LINUX_ECLIPSE_ARCHIVE_NAME
        )
    }

    @Test
    void returnsCorrectEclipseArchiveNameOnWindows() {
        Assume.assumeFalse(
              "Current OS is Linux, skipping Windows test.",
              System.getProperty("os.name").toLowerCase().startsWith("linux")
        )
        Assert.assertEquals(
              "default eclipse archive name on Windows is incorrect!",
              extension.getEclipseArchiveName(),
              TEST_WINDOWS_ECLIPSE_ARCHIVE_NAME
        )
    }
}
