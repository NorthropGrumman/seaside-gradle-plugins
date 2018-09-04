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
package com.ngc.seaside.gradle.plugins.eclipse;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.Internal;

/**
 * Abstract task for tasks that run an application through eclipse.
 * 
 * @param <T> task type
 */
public class AbstractEclipseApplicationTask<T extends AbstractEclipseApplicationTask<T>> extends AbstractExecTask<T> {

   private final RegularFileProperty eclipseExecutable;

   /**
    * Constructor
    * 
    * @param taskType task type
    */
   public AbstractEclipseApplicationTask(Class<T> taskType) {
      super(taskType);
      this.eclipseExecutable = newInputFile();
      getProject().afterEvaluate(__ -> executable(eclipseExecutable.get().getAsFile()));
   }

   /**
    * Returns the property of the eclipse executable file.
    * 
    * @return the property of the eclipse executable file
    */
   @Internal
   public RegularFileProperty getEclipseExecutable() {
      return eclipseExecutable;
   }

}
