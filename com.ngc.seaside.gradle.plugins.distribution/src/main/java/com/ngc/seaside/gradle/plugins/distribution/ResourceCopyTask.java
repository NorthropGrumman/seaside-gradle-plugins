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
package com.ngc.seaside.gradle.plugins.distribution;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Copy;
import org.gradle.execution.commandline.TaskConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

import groovy.lang.Closure;

/**
 * A task extending the {@link Copy} task that includes copying non-file resources, including an {@link InputStream}
 * resource, a {@link URL} resource, and resources from {@link Class#getResource(String) classes} or
 * {@link ClassLoader#getResource(String) class loaders}.
 */
public class ResourceCopyTask extends Copy {

   public static final String RESOURCE_KEY = "resource";
   public static final String NAME_KEY = "name";
   public static final String CLASS_KEY = "class";
   public static final String CLASS_LOADER_KEY = "loader";

   /**
    * Specifies the resource to copy.
    * 
    * <p>
    * The following options are available for the given map:
    * </p>
    * <ul>
    * <li>{@code resource}: The resource to copy. Accepts {@link InputStream}, {@link URL}, or a {@link CharSequence}
    * for {@link Class#getResource(String) class} and {@link ClassLoader#getResource(String) class loader} resources.
    * </li>
    * <li>{@code name}: The filename of the resource. If not provided, the name of the {@code resource} will be used if
    * possible.</li>
    * <li>{@code class}: The {@link Class} to find the {@link CharSequence} {@code resource}.</li>
    * <li>{@code loader}: The {@link ClassLoader} to find the {@link CharSequence} {@code resource} if {@code class} is
    * not provided.</li>
    * </ul>
    * 
    * @param options the options to use to obtain a resource
    * @return this
    */
   public ResourceCopyTask fromResource(Map<String, ?> options) {
      return fromResource(options, a -> {
      });
   }

   /**
    * Specifies the resource to copy and creates a child {@link CopySpec}.
    * 
    * <p>
    * The following options are available for the given map:
    * </p>
    * <ul>
    * <li>{@code resource}: The resource to copy. Accepts {@link InputStream}, {@link URL}, or a {@link CharSequence}
    * for {@link Class#getResource(String) class} and {@link ClassLoader#getResource(String) class loader} resources.
    * <li>{@code name}: The filename of the resource. If not provided and {@code resource} is a {@link CharSequence},
    * the name will be the same as the resource.
    * <li>{@code class}: The {@link Class} to use if the {@code resource} is a {@link CharSequence}.</li>
    * <li>{@code loader}: The {@link ClassLoader} to use if the {@code resource} is a {@link CharSequence} and
    * {@code class} is not provided.</li>
    * </ul>
    * 
    * @param options the options to use to obtain a resource
    * @param c closure for configuring the child {@link CopySpec}
    * @return this
    */
   public ResourceCopyTask fromResource(Map<String, ?> options, Closure<?> c) {
      return fromResource(options, c::call);
   }

   /**
    * Specifies the resource to copy and creates a child {@link CopySpec}.
    * 
    * <p>
    * The following options are available for the given map:
    * </p>
    * <ul>
    * <li>{@code resource}: The resource to copy. Accepts {@link InputStream}, {@link URL}, or a {@link CharSequence}
    * for {@link Class#getResource(String) class} and {@link ClassLoader#getResource(String) class loader} resources.
    * <li>{@code name}: The filename of the resource. If not provided and {@code resource} is a {@link CharSequence},
    * the name will be the same as the resource.
    * <li>{@code class}: The {@link Class} to use if the {@code resource} is a {@link CharSequence}.</li>
    * <li>{@code loader}: The {@link ClassLoader} to use if the {@code resource} is a {@link CharSequence} and
    * {@code class} is not provided. If neither {@code class} or {@code loader} are provided, current thread's
    * class loader will be used.</li>
    * </ul>
    * 
    * @param options the options to use to obtain a resource
    * @param configureAction action for configuring the child {@link CopySpec}
    * @return this
    */
   public ResourceCopyTask fromResource(Map<String, ?> options, Action<? super CopySpec> configureAction) {
      Object resource = options.get(RESOURCE_KEY);
      ClassLoader loader = getClassLoader(options.get(CLASS_LOADER_KEY));
      Optional<Class<?>> cls = getClass(options.get(CLASS_KEY), loader);
      String name = getResourceName(options.get(NAME_KEY), resource);
      Path resourceDir;
      Path resourceFile;
      try (InputStream inputStream = getInputStream(resource, cls, loader)) {
         resourceDir = Files.createTempDirectory(null);
         resourceFile = resourceDir.resolve(name);
         Files.copy(inputStream, resourceFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         throw new TaskConfigurationException(getPath(), "Unable to create file " + name + " from resource", e);
      }
      super.from(resourceFile, configureAction);
      this.doLast(__ -> FileUtils.deleteQuietly(resourceDir.toFile()));
      return this;
   }

   private InputStream getInputStream(Object resource, Optional<Class<?>> cls, ClassLoader classLoader) {
      if (resource == null) {
         throw new TaskConfigurationException(getPath(), "fromResource requires a resource", null);
      }
      if (resource instanceof InputStream) {
         return (InputStream) resource;
      }
      try {
         if (resource instanceof URL) {
            return ((URL) resource).openStream();
         }
         if (resource instanceof URI) {
            return ((URI) resource).toURL().openStream();
         }
      } catch (IOException e) {
         throw new TaskConfigurationException(getPath(), "Unable to get resource", e);
      }
      if (resource instanceof CharSequence) {
         String resourceString = resource.toString();
         if (cls.isPresent()) {
            return cls.get().getResourceAsStream(resourceString);
         }
         return classLoader.getResourceAsStream(resourceString);
      }
      throw new TaskConfigurationException(getPath(), "Invalid resource type: " + resource.getClass(), null);
   }

   private String getResourceName(Object nameObject, Object resource) {
      if (nameObject instanceof CharSequence) {
         return nameObject.toString();
      }
      if (nameObject == null) {
         String path;
         if (resource instanceof CharSequence) {
            path = resource.toString();
         } else if (resource instanceof URL) {
            path = ((URL) resource).getPath();
         } else if (resource instanceof URI) {
            path = ((URI) resource).getPath();
         } else {
            throw new TaskConfigurationException(getPath(), "Cannot determine name from resource " + resource, null);
         }
         return FilenameUtils.getName(path);
      } else {
         throw new TaskConfigurationException(getPath(), "Invalid name type: " + nameObject.getClass(), null);
      }
   }

   private Optional<Class<?>> getClass(Object classObject, ClassLoader classLoader) {
      if (classObject == null) {
         return Optional.empty();
      }
      if (classObject instanceof Class) {
         return Optional.of((Class<?>) classObject);
      }
      if (classObject instanceof CharSequence) {
         String className = classObject.toString();
         try {
            return Optional.of(classLoader.loadClass(className));
         } catch (ClassNotFoundException e) {
            throw new TaskConfigurationException(getPath(), "Could not load resource class", e);
         }
      }
      throw new TaskConfigurationException(getPath(), "Invalid class type: " + classObject.getClass(), null);
   }

   private ClassLoader getClassLoader(Object classLoaderObject) {
      if (classLoaderObject == null) {
         return Thread.currentThread().getContextClassLoader();
      }
      if (classLoaderObject instanceof ClassLoader) {
         return (ClassLoader) classLoaderObject;
      }
      throw new TaskConfigurationException(getPath(), "Invalid class loader type: " + classLoaderObject.getClass(),
         null);
   }
}
