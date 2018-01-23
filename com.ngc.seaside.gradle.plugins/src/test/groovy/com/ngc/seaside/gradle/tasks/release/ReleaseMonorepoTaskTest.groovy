package com.ngc.seaside.gradle.tasks.release

import com.ngc.seaside.gradle.tasks.release.ReleaseMonorepoTask
import org.gradle.api.Project
import java.io.File

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.mockito.Mockito

class ReleaseMonorepoTaskTest {
    private Project project = Mockito.mock(Project)
    private File file = Mockito.mock(File)

    @Before
    void before() {
        Mockito.when(project.buildDir).thenReturn(file)
    }

    @Test
    void doesReleaseWhenAllSubprojectsAreSuccessfullyReleased() {
        Assert.fail("implement me!")
    }

    @Test
    void doesNotReleaseWhenAnySubprojectFailsToBeReleased() {
        Assert.fail("implement me!")
    }

    @Test
    void doesNotAllowReleaseWhenInsideSubprojectDirectory() {
        Assert.fail("implement me!")
    }
}
