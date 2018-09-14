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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class VersionResolverTest {
   private Project project = Mockito.mock(Project, Mockito.RETURNS_DEEP_STUBS)
   private Project rootProject = Mockito.mock(Project)
   private Logger logger = Mockito.mock(Logger)
   private VersionResolver resolver
   private final File file = Mockito.mock(File)

   @Before
   void before() {
      Mockito.when(project.version).thenReturn("1.2.3.DEV")
      Mockito.when(project.rootProject).thenReturn(rootProject)
      Mockito.when(project.extensions.findByType(VersionResolver)).thenReturn(null)
      Mockito.when(project.getLogger()).thenReturn(logger)
      Mockito.when(rootProject.buildFile).thenReturn(file)
      Mockito.when(rootProject.projectDir).thenReturn(file)
      Mockito.when(rootProject.projectDir.parent).thenReturn("")
      resolver = new VersionResolver(project)
   }

   @Test
   void doesGetSemanticVersionFromString() {
      Assert.assertEquals("1.0.0-SNAPSHOT", resolver.getSemanticVersion("version = 1.0.0-SNAPSHOT"))
      Assert.assertEquals("1.0.3.RC", resolver.getSemanticVersion("version = 1.0.3.RC"))
      Assert.assertEquals("1.2.0-TEST", resolver.getSemanticVersion("version =   1.2.0-TEST"))
      Assert.assertEquals("1.2.7-SNAPSHOT", resolver.getSemanticVersion("  version= 1.2.7-SNAPSHOT"))
      Assert.assertEquals("1.5.7-SNAPSHOT", resolver.getSemanticVersion("version=1.5.7-SNAPSHOT"))
   }

   @Test
   void doesGetDefaultVersionFileName() {
      Assert.assertNotNull(resolver.versionFile)
      Assert.assertEquals(file, resolver.versionFile)
   }

   @Test
   void doesGetCustomVersionFileName() {
      Mockito.when(project.hasProperty(ArgumentMatchers.anyString())).thenReturn(true)
      Mockito.when(project.property(ArgumentMatchers.anyString())).thenReturn("custom.gradle")
      resolver = new VersionResolver(project)

      Assert.assertNotNull(resolver.versionFile)
      Assert.assertEquals(file, resolver.versionFile)
   }

   @Test
   void doesGetTagNameWithPrefix() {
      Assert.assertEquals("myPrefix_1.2.3", resolver.getTagName("myPrefix_", ".DEV"))
   }
}
