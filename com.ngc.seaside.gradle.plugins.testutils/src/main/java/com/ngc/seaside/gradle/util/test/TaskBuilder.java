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
package com.ngc.seaside.gradle.util.test;

import org.gradle.api.internal.AbstractTask;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.taskfactory.TaskIdentity;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * Used to build tasks for use in tests.  You use this builder because Gradle doesn't allow you to create instances of
 * outside the DSL.  This builder works around that "feature".  The most common way to use it is:
 *
 * <pre>
 *    MyTask task = new TaskBuilder<MyTask>(MyTask).create()
 * </pre>
 *
 * You can specify your own mocked project like this:
 *
 * <pre>
 *     ProjectInternal myMockedProject = GradleMocks.newProjectMock()
 *     MyTask task = new TaskBuilder<MyTask>(MyTask)
 *       .setProject(myMockedProject)
 *       .create()
 * </pre>
 *
 * Finally, you can control the creation of the task with this:
 *
 * <pre>
 *     MyTask task = new TaskBuilder<MyTask>(MyTask)
 *       .setSupplier({ new MyTask() })
 *       .create()
 * </pre>
 *
 * @param <T> the type of task being built
 */
public class TaskBuilder<T extends AbstractTask> {

   private final Class<T> taskClazz;
   private Supplier<T> taskSupplier;
   private String taskName;
   private ProjectInternal project;

   public TaskBuilder(Class<T> taskClazz) {
      this.taskClazz = taskClazz;
   }

   public TaskBuilder<T> setSupplier(Supplier<T> taskSupplier) {
      this.taskSupplier = taskSupplier;
      return this;
   }

   public TaskBuilder<T> setName(String taskName) {
      this.taskName = taskName;
      return this;
   }

   public TaskBuilder<T> setProject(ProjectInternal project) {
      this.project = project;
      return this;
   }

   public T create() {
      if (taskName == null) {
         taskName = taskClazz.getName();
      }
      if (project == null) {
         project = GradleMocks.newProjectMock();
      }
      if (taskSupplier == null) {
         taskSupplier = this::newInstance;
      }

      return AbstractTask.injectIntoNewInstance(project,
                                                TaskIdentity.create(taskName, taskClazz, project),
                                                () -> taskSupplier.get());
   }

   private T newInstance() {
      try {
         return taskClazz.getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
   }
}
