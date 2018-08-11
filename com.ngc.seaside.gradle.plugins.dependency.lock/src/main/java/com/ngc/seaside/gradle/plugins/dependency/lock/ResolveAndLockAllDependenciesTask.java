package com.ngc.seaside.gradle.plugins.dependency.lock;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskInstantiationException;

public class ResolveAndLockAllDependenciesTask extends DefaultTask {

   public static final String TASK_GROUP = "Dependency Lock";
   public static final String TASK_NAME = "resolveAndLockAllDependencies";
   public static final String TASK_DESCRIPTION = "Resolve all dependencies and write a lock file containing their GAVs";

   @TaskAction
   public void resolveAndLockAllDependencies() {
      doFirst(__ -> {
         if (!getProject().getGradle().getStartParameter().isWriteDependencyLocks()) {
            throw new TaskInstantiationException(String.format("%s task must be run with --write-locks",
                                                               ResolveAndLockAllDependenciesTask.TASK_NAME));
         }
      });

      doLast(__ -> getProject().getConfigurations()
                               .matching(Configuration::isCanBeResolved)
                               .all(Configuration::resolve));
   }
}
