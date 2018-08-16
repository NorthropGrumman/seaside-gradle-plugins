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
