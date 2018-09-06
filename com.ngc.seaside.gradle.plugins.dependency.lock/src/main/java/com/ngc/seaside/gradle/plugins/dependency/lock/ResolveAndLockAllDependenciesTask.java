/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
