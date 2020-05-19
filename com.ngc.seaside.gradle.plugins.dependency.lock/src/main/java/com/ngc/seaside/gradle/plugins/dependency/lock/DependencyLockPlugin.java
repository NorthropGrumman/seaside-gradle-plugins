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

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import org.gradle.api.Project;

public class DependencyLockPlugin extends AbstractProjectPlugin {

   public static final String DEPENDENCY_LOCK_PLUGIN_GROUP = "Dependency Lock";
   public static final String RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME = "resolveAndLockAllDependencies";

   @Override
   protected void doApply(Project project) {
      project.getConfigurations().forEach(c -> c.getResolutionStrategy().activateDependencyLocking());

      project.getTasks().create(
            RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME,
            ResolveAndLockAllDependenciesTask.class,
            task -> {
               task.setGroup(DEPENDENCY_LOCK_PLUGIN_GROUP);
               task.setDescription(ResolveAndLockAllDependenciesTask.DESCRIPTION);
            });
   }
}
