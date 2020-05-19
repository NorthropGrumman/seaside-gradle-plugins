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
package com.ngc.seaside.gradle.plugins.eclipse.updatesite;

import com.ngc.seaside.gradle.plugins.eclipse.AbstractEclipseApplicationTask;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskExecutionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Task for creating the metadata for the Eclipse update site.
 */
public class CreateMetadataTask extends AbstractEclipseApplicationTask<CreateMetadataTask> {

   private final DirectoryProperty updateSiteDirectory;

   /**
    * Constructor.
    */
   public CreateMetadataTask() {
      super(CreateMetadataTask.class);
      this.updateSiteDirectory = newOutputDirectory();
      getArgumentProviders().add(() -> Arrays.asList("-nosplash", "-application",
               "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher", "-compress", "-metadataRepository",
               getUpdateSiteDirUrl().get().toString(), "-artifactRepository", getUpdateSiteDirUrl().get().toString(),
               "-source", getUpdateSiteDirectory().get().toString()));
   }

   /**
    * Returns the property of the directory for the update site.
    * 
    * @return the property of the directory for the update site
    */
   @OutputDirectory
   public DirectoryProperty getUpdateSiteDirectory() {
      return updateSiteDirectory;
   }

   private Provider<URL> getUpdateSiteDirUrl() {
      return updateSiteDirectory.map(file -> file.getAsFile().toURI()).map(uri -> {
         try {
            return uri.toURL();
         } catch (MalformedURLException e) {
            throw new TaskExecutionException(this, e);
         }
      });
   }

}
