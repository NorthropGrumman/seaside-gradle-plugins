package com.ngc.seaside.gradle.plugins.dependency.lock;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskInstantiationException;

public class ResolveAndLockAllDependenciesTask extends DefaultTask {

   public static final String NAME = "resolveAndLockAllDependencies";
   public static final String DESCRIPTION = "Resolve all dependencies and write a lock file containing their GAVs";

   @TaskAction
   public void resolveAndLockAllDependencies() {
      checkForWriteDependencyLocks();
      resolveConfigurationDependencies();
   }

   private void checkForWriteDependencyLocks() {
      if (!getProject().getGradle().getStartParameter().isWriteDependencyLocks()) {
         throw new TaskInstantiationException(String.format("%s task must be run with --write-locks",
                                                            ResolveAndLockAllDependenciesTask.NAME));
      }
   }

   private void resolveConfigurationDependencies() {
      getProject().getConfigurations()
            .matching(Configuration::isCanBeResolved)
            .all(Configuration::resolve);
   }
}
