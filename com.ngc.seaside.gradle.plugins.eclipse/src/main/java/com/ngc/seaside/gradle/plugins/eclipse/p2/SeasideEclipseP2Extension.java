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
package com.ngc.seaside.gradle.plugins.eclipse.p2;

import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipseExtension;
import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeasideEclipseP2Extension {

   private final Project project;
   private final Map<String, ExternalP2Repository> externalUpdateSites = new LinkedHashMap<>();
   private final Provider<Directory> cacheDirectory;

   /**
    * Constructor.
    * 
    * @param project project
    */
   public SeasideEclipseP2Extension(Project project) {
      this.project = project;
      this.cacheDirectory = ((BaseEclipseExtension) project.getExtensions().getByName(BaseEclipsePlugin.EXTENSION_NAME))
               .getCacheDirectory();
   }

   /**
    * Returns the external update sites.
    * 
    * @return the external update sites
    */
   public Collection<ExternalP2Repository> getExternalUpdateSites() {
      return externalUpdateSites.values();
   }

   /**
    * Executes the given action for configuring this extension using the given external update site.
    * 
    * @param url update site url
    * @param action action
    * @return the configuration
    */
   public ExternalP2Repository remoteRepository(String url,
            Action<ExternalP2Repository> action) {
      ExternalP2Repository configuration =
               externalUpdateSites.computeIfAbsent(url, __ -> new ExternalP2Repository(project, url));
      configuration.getCacheDirectory().set(cacheDirectory);
      action.execute(configuration);
      return configuration;
   }

   /**
    * Executes the given closure for configuring this extension using the given external update site.
    * 
    * @param url update site url
    * @param closure closure
    * @return the configuration
    */
   public ExternalP2Repository remoteRepository(String url, Closure<?> closure) {
      ExternalP2Repository configuration =
               externalUpdateSites.computeIfAbsent(url, __ -> new ExternalP2Repository(project, url));
      configuration.getCacheDirectory().set(cacheDirectory);
      closure.setDelegate(configuration);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(configuration);
      return configuration;
   }

   void processExternalUpdateSites() {
      for (ExternalP2Repository config : externalUpdateSites.values()) {
         config.configure();
      }
   }

}
