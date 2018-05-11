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
