package com.ngc.seaside.gradle.util

import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.tasks.release.ReleaseType
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VersionResolverIT {
   private File projectDir
   private Project project
   private SeasideReleasePlugin plugin
   private VersionResolver resolver

   @Before
   void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
      )
      project = TestingUtilities.createTheTestProjectWith(projectDir)

      resolver = new VersionResolver(project)
      resolver.enforceVersionSuffix = false

      plugin = new SeasideReleasePlugin()
      plugin.apply(project)
   }

   @Test
   void doesUpdateProjectVersionCorrectlyBasedOnReleaseType() {
      def version = "4.5.6"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals("4.5.7", resolver.getUpdatedProjectVersionForRelease(ReleaseType.PATCH))
      Assert.assertEquals("4.6.0", resolver.getUpdatedProjectVersionForRelease(ReleaseType.MINOR))
      Assert.assertEquals("5.0.0", resolver.getUpdatedProjectVersionForRelease(ReleaseType.MAJOR))

      version = "4.5.6-SUFFIX"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals(version, resolver.getUpdatedProjectVersionForRelease(ReleaseType.SNAPSHOT))
   }

   @Test
   void doesGetCurrentProjectVersion() {
      def version = "1.2.3"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals(
            "the current version should have been: $version!",
            version,
            resolver.getProjectVersion()
      )

      version = "1.2.3-SNAPSHOT"
      resolver.setProjectVersionOnFile(version)
      Assert.assertEquals(
            "the current version should have been: $version!",
            version,
            resolver.getProjectVersion()
      )
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
            "src", "integrationTest", "resources", "sealion-java-hello-world"
      )
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
            "build", "integrationTest", "resources", "util", "sealion-java-hello-world"
      )
   }
}
