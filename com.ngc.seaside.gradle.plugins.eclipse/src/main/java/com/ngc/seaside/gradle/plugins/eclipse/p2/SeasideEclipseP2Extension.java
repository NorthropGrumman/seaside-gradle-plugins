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
