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
package com.ngc.seaside.gradle.util;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class TaskResolver implements IResolver {

   private Project project;

   public TaskResolver(Project project) {
      this.project = project;
   }

   public Task findTask(String taskName) {
      return project.getTasks().getByName(taskName);
   }

   public Task findTask(String taskName, Closure<?> closure) {
      return this.project.getTasks().getByName(taskName, closure);
   }

   public Task findOptionalTask(String taskName) {
      return findOptionalTask(project, taskName);
   }

   public Task findOptionalTask(String taskName, Closure<?> closure) {
      return findOptionalTask(project, taskName, closure);
   }

   public static Task findTask(Project project, String taskName) {
      return project.getTasks().getByName(taskName);
   }

   public static Task findTask(Project project, String taskName, Closure<?> closure) {
      return project.getTasks().getByName(taskName, closure);
   }

   public static Task findOptionalTask(Project project, String taskName) {
      return project.getTasks().findByName(taskName);
   }

   public static Task findOptionalTask(Project project, String taskName, Closure<?> closure) {
      Task task = project.getTasks().findByName(taskName);
      if (task != null) {
         task.configure(closure);
      }
      return task;
   }
}
