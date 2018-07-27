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
