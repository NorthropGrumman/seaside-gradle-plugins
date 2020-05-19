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
