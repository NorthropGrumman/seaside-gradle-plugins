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
