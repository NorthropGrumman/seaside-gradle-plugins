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
package com.ngc.seaside.gradle.plugins.dependency.lock;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.ngc.seaside.gradle.util.test.TestingUtilities.assertTaskSuccess;

public class DependencyLockPluginFT {

   private File projectDir;

   @Before
   public void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
      );
      TestingUtilities.createTheTestProjectWith(projectDir);
   }

   @Test
   public void doesThrowExceptionWhenWriteLocksFlagNotProvided() {
      BuildResult result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME)
            .buildAndFail();

      Assert.assertTrue(
            "did not receive expected exception!",
            result.getOutput()
                  .contains(String.format("%s task must be run with --write-locks",
                                          DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME)));
   }

   @Test
   public void doesSuccessfullyWriteLockFiles() {
      BuildResult result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build", "install")
            .build();

      assertTaskSuccess(result, "hello", "install");
      assertTaskSuccess(result, "goodbye", "install");

      result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME, "--write-locks")
            .build();

      assertTaskSuccess(result, "hello", DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME);
      assertTaskSuccess(result, "goodbye", DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME);

      result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build", "--offline")
            .build();

      assertTaskSuccess(result, "hello", "build");
      assertTaskSuccess(result, "goodbye", "build");
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "sealion-java-hello-world-monorepo"
      );
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "dependency-lock", "sealion-java-hello-world-monorepo"
      );
   }
}
