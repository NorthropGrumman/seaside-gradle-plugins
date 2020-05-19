/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
