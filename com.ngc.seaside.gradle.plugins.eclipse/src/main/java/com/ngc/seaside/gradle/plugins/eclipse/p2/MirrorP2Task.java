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
package com.ngc.seaside.gradle.plugins.eclipse.p2;

import com.ngc.seaside.gradle.plugins.eclipse.AbstractEclipseApplicationTask;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import java.util.Arrays;

public class MirrorP2Task extends AbstractEclipseApplicationTask<MirrorP2Task> {

   private DirectoryProperty p2Directory;
   private Property<String> url;

   /**
    * Constructor.
    */
   public MirrorP2Task() {
      super(MirrorP2Task.class);
      this.p2Directory = newOutputDirectory();
      this.url = getProject().getObjects().property(String.class);
      getArgumentProviders().add(() -> Arrays.asList("-nosplash", "-application",
               "org.eclipse.equinox.p2.artifact.repository.mirrorApplication",
               "-source", url.get(), "-destination", p2Directory.get().getAsFile().toString()));
   }

   /**
    * Returns the property of the directory for mirroring the p2 repository.
    * 
    * @return the property of the directory for mirroring the p2 repository
    */
   @Internal
   public DirectoryProperty getP2Directory() {
      return p2Directory;
   }

   /**
    * Returns the property of the p2 repository url.
    * 
    * @return the property of the p2 repository url
    */
   @Input
   public Property<String> getP2Url() {
      return url;
   }
}
