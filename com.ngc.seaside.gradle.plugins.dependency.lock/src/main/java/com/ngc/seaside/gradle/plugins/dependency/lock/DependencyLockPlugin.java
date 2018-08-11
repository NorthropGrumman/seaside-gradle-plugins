package com.ngc.seaside.gradle.plugins.dependency.lock;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class DependencyLockPlugin extends AbstractProjectPlugin {

   @Override
   protected void doApply(Project project) {
      project.getConfigurations().forEach(c -> c.getResolutionStrategy().activateDependencyLocking());

      Map<String, String> options = new HashMap<>();
      options.put("name", ResolveAndLockAllDependenciesTask.TASK_NAME);
      options.put("group", ResolveAndLockAllDependenciesTask.TASK_GROUP);
      options.put("description", ResolveAndLockAllDependenciesTask.TASK_DESCRIPTION);

      project.getTasks().create(options);
   }
}
