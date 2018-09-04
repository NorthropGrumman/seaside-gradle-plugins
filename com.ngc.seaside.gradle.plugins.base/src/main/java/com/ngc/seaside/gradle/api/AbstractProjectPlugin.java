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
package com.ngc.seaside.gradle.api;

import com.ngc.seaside.gradle.util.TaskResolver;

import org.gradle.api.Project;

public abstract class AbstractProjectPlugin implements IProjectPlugin {

   private TaskResolver taskResolver;

   /**
    * Inject project version configuration and force subclasses to use it
    * 
    * @param project project applying this plugin
    */
   @Override
   public void apply(Project project) {
      taskResolver = new TaskResolver(project);
      doApply(project);
   }

   @Override
   public TaskResolver getTaskResolver() {
      return taskResolver;
   }

   /**
    * Default action for applying a project plugin.
    * 
    * @param project project applying this plugin
    */
   protected abstract void doApply(Project project);

}
