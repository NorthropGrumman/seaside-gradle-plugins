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
