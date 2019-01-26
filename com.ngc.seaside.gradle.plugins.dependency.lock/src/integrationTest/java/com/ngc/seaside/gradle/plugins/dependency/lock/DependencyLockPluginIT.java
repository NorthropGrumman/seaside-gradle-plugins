/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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

import com.ngc.seaside.gradle.util.TaskResolver;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class DependencyLockPluginIT {

   private TaskResolver resolver;

   @Before
   public void before() {
      File projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
      );

      Project project = TestingUtilities.createTheTestProjectWith(projectDir);

      DependencyLockPlugin plugin = new DependencyLockPlugin();
      plugin.doApply(project);

      resolver = new TaskResolver(project);
   }

   @Test
   public void doesApplyDependencyLockPlugin() {
      Task task = resolver.findTask(DependencyLockPlugin.RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME);
      Assert.assertNotNull(task);
      Assert.assertEquals("task group is incorrect!",
                          DependencyLockPlugin.DEPENDENCY_LOCK_PLUGIN_GROUP,
                          task.getGroup());
      Assert.assertEquals("task description is incorrect!",
                          ResolveAndLockAllDependenciesTask.DESCRIPTION,
                          task.getDescription());
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
            "src", "integrationTest", "resources", "sealion-java-hello-world-monorepo"
      );
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
            "build", "integrationTest", "resources", "dependency-lock", "sealion-java-hello-world-monorepo"
      );
   }
}
