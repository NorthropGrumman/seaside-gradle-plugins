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
package com.ngc.seaside.gradle.plugins.version

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class VersionResolverTest {

   private Project project = mock(Project, Mockito.RETURNS_DEEP_STUBS)
   private Project rootProject = mock(Project)
   private Logger logger = mock(Logger)
   private VersionResolver resolver
   private final File file = mock(File)

   @Before
   void before() {
      when(project.version).thenReturn("1.2.3.DEV")
      when(project.rootProject).thenReturn(rootProject)
      when(project.extensions.findByType(VersionResolver)).thenReturn(null)
      when(project.getLogger()).thenReturn(logger)
      when(rootProject.buildFile).thenReturn(file)
      when(rootProject.projectDir).thenReturn(file)
      when(rootProject.projectDir.parent).thenReturn("")
      resolver = new VersionResolver(project)
   }

   @Test
   void doesGetSemanticVersionFromString() {
      assertEquals("1.0.0-SNAPSHOT", resolver.getSemanticVersion("version = 1.0.0-SNAPSHOT"))
      assertEquals("1.0.3.RC", resolver.getSemanticVersion("version = 1.0.3.RC"))
      assertEquals("1.2.0-TEST", resolver.getSemanticVersion("version =   1.2.0-TEST"))
      assertEquals("1.2.7-SNAPSHOT", resolver.getSemanticVersion("  version= 1.2.7-SNAPSHOT"))
      assertEquals("1.5.7-SNAPSHOT", resolver.getSemanticVersion("version=1.5.7-SNAPSHOT"))
   }

   @Test
   void doesGetDefaultVersionFileName() {
      assertNotNull(resolver.versionFile)
      assertEquals(file, resolver.versionFile)
   }

   @Test
   void doesGetCustomVersionFileName() {
      when(project.hasProperty(anyString())).thenReturn(true)
      when(project.property(anyString())).thenReturn("custom.gradle")
      resolver = new VersionResolver(project)

      assertNotNull(resolver.versionFile)
      assertEquals(file, resolver.versionFile)
   }

   @Test
   void doesGetTagNameWithPrefix() {
      assertEquals("myPrefix_1.2.3", resolver.getTagName("myPrefix_", ".DEV"))
   }
}
