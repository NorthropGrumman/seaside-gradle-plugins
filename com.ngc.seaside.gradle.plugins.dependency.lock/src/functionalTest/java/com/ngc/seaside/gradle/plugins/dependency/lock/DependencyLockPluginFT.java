package com.ngc.seaside.gradle.plugins.dependency.lock;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.api.tasks.TaskInstantiationException;
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

   @Test(expected = TaskInstantiationException.class)
   public void doesThrowExceptionWhenWriteLocksFlagNotProvided() {
      SeasideGradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", ResolveAndLockAllDependenciesTask.TASK_NAME)
            .build();
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
