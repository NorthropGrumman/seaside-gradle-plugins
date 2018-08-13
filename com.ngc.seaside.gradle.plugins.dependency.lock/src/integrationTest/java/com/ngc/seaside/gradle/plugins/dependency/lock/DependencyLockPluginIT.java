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
