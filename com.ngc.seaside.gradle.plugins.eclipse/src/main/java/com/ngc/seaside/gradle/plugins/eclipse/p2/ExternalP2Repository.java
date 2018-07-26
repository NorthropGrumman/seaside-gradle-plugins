package com.ngc.seaside.gradle.plugins.eclipse.p2;

import com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature.EclipseFeature;
import com.ngc.seaside.gradle.util.OsgiResolver;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Provider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.jar.Manifest;

public class ExternalP2Repository {

   private final Project project;
   private final NamedDomainObjectContainer<EclipseFeature> features;
   private final NamedDomainObjectContainer<ExternalPlugin> plugins;
   private final String url;
   private final DirectoryProperty cacheDirectory;

   /**
    * Constructor
    * 
    * @param project project
    * @param url update site url
    */
   public ExternalP2Repository(Project project, String url) {
      this.project = project;
      this.cacheDirectory = project.getLayout().directoryProperty();
      this.url = url;
      this.features = project.container(EclipseFeature.class);
      this.plugins = project.container(ExternalPlugin.class);
   }

   /**
    * Returns the url to the p2 repository.
    * 
    * @return the url to the p2 repository
    */
   public String getUrl() {
      return url;
   }

   /**
    * Returns a url that can be used for file names.
    * 
    * @return a url that can be used for file names
    */
   String getCleanUrl() {
      String name;
      try {
         URL u = new URL(url);
         name = u.getHost() + "_" + u.getPath().replace('/', '_');
      } catch (MalformedURLException e) {
         throw new IllegalArgumentException(e);
      }
      return name;
   }

   /**
    * Adds an action that will be executed for every feature in the update site.
    * 
    * @param action action
    * @return this
    */
   public ExternalP2Repository features(Action<EclipseFeature> action) {
      features.all(action);
      return this;
   }

   /**
    * Adds a closure that will be called for every feature in the update site.
    * 
    * @param closure closure
    * @return this
    */
   public ExternalP2Repository features(Closure<?> closure) {
      features.all(closure);
      return this;
   }

   /**
    * Adds an action that will be called for every plugin in the update site.
    * 
    * @param action action
    * @return this
    */
   public ExternalP2Repository plugins(Action<ExternalPlugin> action) {
      plugins.all(action);
      return this;
   }

   /**
    * Adds a closure that will be called for the manifest of every plugin in the update site.
    * 
    * @param closure closure
    * @return this
    */
   public ExternalP2Repository plugins(Closure<?> closure) {
      plugins.all(closure);
      return this;
   }

   /**
    * Returns the property of the eclipse cache directory.
    * 
    * @return the property of the eclipse cache directory
    */
   public DirectoryProperty getCacheDirectory() {
      return cacheDirectory;
   }

   private Provider<Directory> getMirrorsCacheDirectory() {
      return getCacheDirectory().dir("mirrors");
   }

   /**
    * Returns the provider of the directory for caching the p2 site.
    * 
    * @return the provider of the directory for caching the p2 site
    */
   public Provider<Directory> getCacheMirrorSiteDirectory() {
      String name = getCleanUrl();
      return getMirrorsCacheDirectory().map(dir -> dir.dir(name));
   }

   void configure() {
      Path mirrorSite = getCacheMirrorSiteDirectory().get().getAsFile().toPath();
      try {
         Files.list(mirrorSite.resolve("features")).map(Path::toAbsolutePath)
                  .map(ExternalP2Repository::fromJar).forEach(features::add);
         Files.list(mirrorSite.resolve("plugins")).map(Path::toAbsolutePath).map(ExternalPlugin::new)
                  .forEach(plugins::add);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private static EclipseFeature fromJar(Path jar) {
      URI fileUri = jar.toUri();
      URI zipUri;
      try {
         zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
      } catch (URISyntaxException e) {
         throw new IllegalArgumentException(e);
      }
      try (FileSystem fs = FileSystems.newFileSystem(zipUri, Collections.singletonMap("create", "true"))) {
         return EclipseFeature.fromXml(Files.newBufferedReader(fs.getPath("feature.xml")));
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /**
    * Represents a plugin for an external update site.
    */
   public class ExternalPlugin {

      private Path jar;
      private Manifest manifest;

      /**
       * Constructor
       * 
       * @param jar plugin jar
       */
      private ExternalPlugin(Path jar) {
         this.jar = jar;
         URI fileUri = jar.toUri();
         URI zipUri;
         try {
            zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
         } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
         }
         try (FileSystem fs = FileSystems.newFileSystem(zipUri, Collections.singletonMap("create", "true"))) {
            this.manifest = new Manifest(Files.newInputStream(fs.getPath("META-INF", "MANIFEST.MF")));
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }

      public Path getJar() {
         return jar;
      }

      public Object getDependency() {
         return project.files(jar);
      }

      public Manifest getManifest() {
         return manifest;
      }

      public String getName() {
         return OsgiResolver.getOsgiSymbolicName(manifest).get();
      }

      public String getVersion() {
         return OsgiResolver.getOsgiVersion(manifest);
      }
   }
}
