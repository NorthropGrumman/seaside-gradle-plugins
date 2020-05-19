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

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskInstantiationException;

public class ResolveAndLockAllDependenciesTask extends DefaultTask {

   public static final String DESCRIPTION = "Resolve all dependencies and write a lock file containing their GAVs";

   @TaskAction
   public void resolveAndLockAllDependencies() {
      checkForWriteDependencyLocks();
      resolveConfigurationDependencies();
   }

   private void checkForWriteDependencyLocks() {
      if (!getProject().getGradle().getStartParameter().isWriteDependencyLocks()) {
         throw new TaskInstantiationException(String.format("%s task must be run with --write-locks", getName()));
      }
   }

   private void resolveConfigurationDependencies() {
      getProject().getConfigurations()
            .matching(Configuration::isCanBeResolved)
            .all(Configuration::resolve);
   }
}
