package com.ngc.seaside.gradle.plugins.dependency.lock;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

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
            .withArguments(ResolveAndLockAllDependenciesTask.NAME)
            .buildAndFail();

      Assert.assertTrue(
            "did not receive expected exception!",
            result.getOutput()
                  .contains(ResolveAndLockAllDependenciesTask.NAME + " task must be run with --write-locks"));
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

      TestingUtilities.assertTaskSuccess(result, "hello", "install");
      TestingUtilities.assertTaskSuccess(result, "goodbye", "install");

      result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(ResolveAndLockAllDependenciesTask.NAME, "--write-locks")
            .build();

      TestingUtilities.assertTaskSuccess(result, "hello", ResolveAndLockAllDependenciesTask.NAME);
      TestingUtilities.assertTaskSuccess(result, "goodbye", ResolveAndLockAllDependenciesTask.NAME);

      result = SeasideGradleRunner.create()
            .withNexusProperties()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", "build", "--offline")
            .build();

      TestingUtilities.assertTaskSuccess(result, "hello", "build");
      TestingUtilities.assertTaskSuccess(result, "goodbye", "build");
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
