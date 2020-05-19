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
package com.ngc.seaside.gradle.api;

import com.ngc.seaside.gradle.util.TaskResolver;

import org.gradle.api.Project;

public abstract class AbstractProjectPlugin implements IProjectPlugin {

   private TaskResolver taskResolver;

   /**
    * Inject project version configuration and force subclasses to use it
    * 
    * @param project project applying this plugin
    */
   @Override
   public void apply(Project project) {
      taskResolver = new TaskResolver(project);
      doApply(project);
   }

   @Override
   public TaskResolver getTaskResolver() {
      return taskResolver;
   }

   /**
    * Default action for applying a project plugin.
    * 
    * @param project project applying this plugin
    */
   protected abstract void doApply(Project project);

}
