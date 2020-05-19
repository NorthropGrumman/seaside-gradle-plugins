/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
