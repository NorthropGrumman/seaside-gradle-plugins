package com.ngc.seaside.gradle.plugins.util

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class VersionResolverTest {
   private Project project = Mockito.mock(Project)
   private Project rootProject = Mockito.mock(Project)
   private Logger logger = Mockito.mock(Logger)
   private VersionResolver resolver
   private final File file = Mockito.mock(File)

   @Before
   void before() {
      Mockito.when(project.version).thenReturn("1.2.3.DEV")
      Mockito.when(project.rootProject).thenReturn(rootProject)
      Mockito.when(project.getLogger()).thenReturn(logger)
      Mockito.when(rootProject.projectDir).thenReturn(file)
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
   void doesGetVersionFileName() {
      Assert.assertNotNull(resolver.versionFile)
      Assert.assertEquals('build.gradle', resolver.versionFile.getName())
   }

   @Test
   void doesGetTagNameWithPrefix() {
      Assert.assertEquals('myPrefix_1.2.3', resolver.getTagName('myPrefix_', '.DEV'))
   }
}
