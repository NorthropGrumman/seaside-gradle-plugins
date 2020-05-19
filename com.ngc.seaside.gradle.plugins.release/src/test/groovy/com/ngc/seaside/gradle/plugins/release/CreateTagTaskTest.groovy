/*
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.ngc.seaside.gradle.plugins.release

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import com.ngc.seaside.gradle.plugins.release.task.CreateTagTask
import com.ngc.seaside.gradle.plugins.version.IVersionUpgradeStrategy
import com.ngc.seaside.gradle.plugins.version.VersionResolver
import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder

import org.gradle.api.internal.project.ProjectInternal
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


/**
 * Test for the CreateTagTask
 */
@RunWith(MockitoJUnitRunner.Silent)
class CreateTagTaskTest {
    private static final String TEST_PREFIX ="v"
    private static final String TEST_RELEASE_MINOR_VERSION = "1.3.0"

    private ProjectInternal testProject
    private CreateTagTask task

    @Mock
    private IVersionUpgradeStrategy upgradeStrategy

    @Mock
    private VersionResolver resolver

    @Before
    void before() {
        testProject = GradleMocks.newProjectMock()
        when(testProject.rootProject).thenReturn(testProject)
        when(testProject.rootProject.hasProperty("releaseVersion")).thenReturn(true)
        when(resolver.getProjectVersion()).thenReturn(TEST_RELEASE_MINOR_VERSION)

    }

    @Test
    void canUpgradeVersionForPatchRelease() {
        confirmVersionUpgradeForReleaseType(TEST_PREFIX + TEST_RELEASE_MINOR_VERSION)
    }

    private void confirmVersionUpgradeForReleaseType(String expectedUpgradeVersion) {
        task = createTask()
        task.setTagName()
        verify(resolver).getProjectVersion()
        Assert.assertEquals(expectedUpgradeVersion, task.getTagName())
    }

    private CreateTagTask createTask() {
        return new TaskBuilder<CreateTagTask>(CreateTagTask)
              .setProject(testProject)
              .setName(SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME)
              .setSupplier({ new CreateTagTask(resolver, TEST_PREFIX) })
              .create()
    }
}
