package com.ngc.seaside.gradle.tasks;

import com.google.common.base.Preconditions;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;

/**
 * A generic {@code Action} that is a {@code Task}.  A task may contain several actions.  Actions may extend this
 * class.
 *
 * <p/>
 *
 * Actions are not meant to be re-usable.  Rather, actions are used to contain procedural logic and reduce the
 * complexity of the task they are associated with.  Tasks may delegate to actions to perform the majority of their
 * functionality.
 *
 * @param <T> the type of {@code Task} this action is associated with
 */
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

   /**
    * Invoked to verify the task associated with this action is configured correctly.  If this action determines some
    * configuration is invalid, it may throw {@code InvalidUserDataException}s
    *
    * @param task the task associated with this action
    * @throws InvalidUserDataException if the task assoicated with this action is not configured correctly
    */
   public void validate(T task) throws InvalidUserDataException {
      // Default behavior is to do nothing.
   }

   /**
    * Invoked to perform the actual execution of the action.
    */
   protected abstract void doExecute();

   /**
    * Invoked prior to executing the action.
    */
   protected void preExecute(T task) {
      this.task = Preconditions.checkNotNull(task, "task may not be null!");
      this.logger = this.task.getLogger();
   }

   /**
    * Invoked after executing the action.
    */
   protected void postExecute() {
      this.task = null;
      this.logger = null;
   }
}
