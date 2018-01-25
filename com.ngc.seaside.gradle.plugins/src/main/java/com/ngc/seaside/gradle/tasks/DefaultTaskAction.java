package com.ngc.seaside.gradle.tasks;

import com.google.common.base.Preconditions;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;

public abstract class DefaultTaskAction<T extends Task> implements Action<T> {

   /**
    * The task that is configured to use this action.
    */
   protected T task;

   /**
    * A logger this task can use.
    */
   protected Logger logger;

   @Override
   public void execute(T task) {
      preExecute(task);
      doExecute();
      postExecute();
   }

   public void validate(T task) throws InvalidUserDataException {
      // Default behavior is to do nothing.
   }

   protected abstract void doExecute();

   protected void preExecute(T task) {
      this.task = Preconditions.checkNotNull(task, "task may not be null!");
      this.logger = this.task.getLogger();
   }

   protected void postExecute() {
   }
}
