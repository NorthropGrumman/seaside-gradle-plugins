package com.ngc.seaside.gradle.plugins.dependency.lock;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ComponentSelection;
import org.gradle.api.artifacts.ResolutionStrategy;

public class DependencyLockPlugin extends AbstractProjectPlugin {

   public static final String DEPENDENCY_LOCK_PLUGIN_GROUP = "Dependency Lock";
   public static final String RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME = "resolveAndLockAllDependencies";

   @Override
   protected void doApply(Project project) {
      project.getConfigurations().forEach(c -> c.resolutionStrategy(this::configureResolutionStrategy));

      project.getTasks().create(
            RESOLVE_AND_LOCK_DEPENDENCIES_TASK_NAME,
            ResolveAndLockAllDependenciesTask.class,
            task -> {
               task.setGroup(DEPENDENCY_LOCK_PLUGIN_GROUP);
               task.setDescription(ResolveAndLockAllDependenciesTask.DESCRIPTION);
            });
   }

   private void configureResolutionStrategy(ResolutionStrategy strategy) {
      strategy.activateDependencyLocking();
      strategy.preferProjectModules();
      strategy.componentSelection(rules -> rules.all(this::rejectLockingCompanyComponents));
   }

   private void rejectLockingCompanyComponents(ComponentSelection selection) {
      if (selection.getCandidate().getGroup().startsWith("com.ngc.seaside")) {
         selection.reject("don't include CEACIDE products");
      }
   }
}
