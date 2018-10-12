/**
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
package com.ngc.seaside.gradle.plugins.repository;

import static org.junit.Assert.fail;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class SeasideRepositoryPluginPublishFT {
   private File projectDir;

   @Before
   public void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
         sourceDirectoryWithTheTestProject(),
         pathToTheDestinationProjectDirectory());
   }

   @Test
   public void canPublish() {
      SeasideGradleRunner runner = SeasideGradleRunner.create()
                                                      .withNexusProperties()
                                                      .withProjectDir(projectDir)
                                                      .withPluginClasspath()
                                                      .forwardOutput()
                                                      .withArguments("clean",
                                                         "build",
                                                         "publishToMavenLocal",
                                                         "publish",
                                                         "-S",
                                                         "--info");

      try {
         BuildResult result = runner.build();
         TestingUtilities.assertTaskSuccess(result, null, "publishToMavenLocal");
         TestingUtilities.assertTaskSuccess(result, null, "publish");
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
         "publish-test");
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
         "build",
         "functionalTest",
         "repository",
         "publish-test");
   }
}
