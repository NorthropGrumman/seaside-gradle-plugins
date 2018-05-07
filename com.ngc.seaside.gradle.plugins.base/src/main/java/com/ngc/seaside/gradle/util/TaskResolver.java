package com.ngc.seaside.gradle.util;

import org.gradle.api.Project;
import org.gradle.api.Task;

import groovy.lang.Closure;

public class TaskResolver implements IResolver {

   private Project project;

   public TaskResolver(Project project) {
       this.project = project;
   }

   public Task findTask(String taskName) {
       return project.getTasks().getByName(taskName);
   }

   public static Task findTask(Project project, String taskName) {
       return project.getTasks().getByName(taskName);
   }

   public Task findTask(String taskName, Closure<?> closure) {
       return this.project.getTasks().getByName(taskName, closure);
   }

   public static Task findTask(Project project, String taskName, Closure<?> closure) {
       return project.getTasks().getByName(taskName, closure);
   }
   
}
