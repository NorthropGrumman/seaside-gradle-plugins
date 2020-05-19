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
package com.ngc.seaside.gradle.plugins.eclipse;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.AbstractExecTask;
import org.gradle.api.tasks.Internal;

/**
 * Abstract task for tasks that run an application through eclipse.
 * 
 * @param <T> task type
 */
public class AbstractEclipseApplicationTask<T extends AbstractEclipseApplicationTask<T>> extends AbstractExecTask<T> {

   private final RegularFileProperty eclipseExecutable;

   /**
    * Constructor
    * 
    * @param taskType task type
    */
   public AbstractEclipseApplicationTask(Class<T> taskType) {
      super(taskType);
      this.eclipseExecutable = newInputFile();
      getProject().afterEvaluate(__ -> executable(eclipseExecutable.get().getAsFile()));
   }

   /**
    * Returns the property of the eclipse executable file.
    * 
    * @return the property of the eclipse executable file
    */
   @Internal
   public RegularFileProperty getEclipseExecutable() {
      return eclipseExecutable;
   }

}
