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
package com.ngc.seaside.gradle.util;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskResolverTest {

   private TaskResolver resolver;

   @Mock
   private Task task;

   @Mock
   private TaskContainer taskContainer;

   @Mock
   private Project project;

   @Before
   public void setup() throws Throwable {
      when(project.getTasks()).thenReturn(taskContainer);

      resolver = new TaskResolver(project);
   }

   @Test
   public void testDoesApplyClosure() throws Throwable {
      String taskName = "foo";
      @SuppressWarnings({"unchecked"})
      Closure<Task> closure = mock(Closure.class);
      when(taskContainer.getByName(taskName, closure)).thenReturn(task);

      assertEquals("did not return task!",
                   task,
                   resolver.findTask(taskName, closure));
      verify(taskContainer).getByName(taskName, closure);
   }

   @Test
   public void testDoesApplyClosureWhenInvokedStatically() throws Throwable {
      String taskName = "foo";
      @SuppressWarnings({"unchecked"})
      Closure<Task> closure = mock(Closure.class);
      when(taskContainer.getByName(taskName, closure)).thenReturn(task);

      assertEquals("did not return task!",
                   task,
                   TaskResolver.findTask(project, taskName, closure));
      verify(taskContainer).getByName(taskName, closure);
   }
}
