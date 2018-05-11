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
