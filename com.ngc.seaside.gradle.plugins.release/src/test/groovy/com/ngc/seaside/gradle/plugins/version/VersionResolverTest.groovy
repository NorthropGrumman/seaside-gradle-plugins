/*
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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
