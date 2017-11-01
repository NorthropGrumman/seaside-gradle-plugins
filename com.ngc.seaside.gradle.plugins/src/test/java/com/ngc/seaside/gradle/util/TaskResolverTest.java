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
      Closure<Task> closure = mock(Closure.class);
      when(taskContainer.getByName(taskName, closure)).thenReturn(task);

      assertEquals("did not return task!",
                   task,
                   TaskResolver.findTask(project, taskName, closure));
      verify(taskContainer).getByName(taskName, closure);
   }
}
