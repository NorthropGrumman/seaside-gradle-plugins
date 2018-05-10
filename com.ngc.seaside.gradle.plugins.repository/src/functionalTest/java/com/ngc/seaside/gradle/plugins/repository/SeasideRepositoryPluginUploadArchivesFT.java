package com.ngc.seaside.gradle.plugins.repository;

import static org.junit.Assert.fail;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class SeasideRepositoryPluginUploadArchivesFT {
   private File projectDir;

   @Before
   public void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
         sourceDirectoryWithTheTestProject(),
         pathToTheDestinationProjectDirectory());
   }

   @Test
   public void canUpload() {
      SeasideGradleRunner runner = SeasideGradleRunner.create()
                                                      .withNexusProperties()
                                                      .withProjectDir(projectDir)
                                                      .withPluginClasspath()
                                                      .forwardOutput()
                                                      .withArguments("clean",
                                                         "build",
                                                         "install",
                                                         "uploadArchives",
                                                         "-S",
                                                         "--info");

      try {
         BuildResult result = runner.build();
         TestingUtilities.assertTaskSuccess(result, null, "uploadArchives");
      } catch (Exception e) {
         e.printStackTrace(new PasswordHidingWriter(System.err));
         fail();
      }
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
         "src",
         "functionalTest",
         "resources",
         "upload-test");
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
         "build",
         "functionalTest",
         "repository",
         "upload-test");
   }
}
