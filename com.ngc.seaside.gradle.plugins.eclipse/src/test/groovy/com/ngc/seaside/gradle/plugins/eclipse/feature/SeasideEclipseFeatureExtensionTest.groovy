package com.ngc.seaside.gradle.plugins.eclipse.feature

import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class SeasideEclipseFeatureExtensionTest {
    private static final String TEST_PROJECT_VERSION = "1.2.3-SNAPSHOT"
    private static final String TEST_PROJECT_GROUP = "test.project.group"
    private static final String TEST_PROJECT_NAME = "test-project-name"
    private static final String TEST_ARCHIVE_NAME =
          "${TEST_PROJECT_GROUP}.${TEST_PROJECT_NAME}-${TEST_PROJECT_VERSION}.jar"

    private SeasideEclipseFeatureExtension extension

    @Mock
    private Project project

    @Before
    void before() {
        when(project.group).thenReturn(TEST_PROJECT_GROUP)
        when(project.name).thenReturn(TEST_PROJECT_NAME)
        when(project.version).thenReturn(TEST_PROJECT_VERSION)

        extension = new SeasideEclipseFeatureExtension(project)
    }

    @Test
    void hasArchiveNameProperty() {
        Assert.assertNotNull("archiveName property doesn't exist!", extension.archiveName)
        Assert.assertEquals("default archive name is incorrect!", extension.archiveName, TEST_ARCHIVE_NAME)
    }
}
