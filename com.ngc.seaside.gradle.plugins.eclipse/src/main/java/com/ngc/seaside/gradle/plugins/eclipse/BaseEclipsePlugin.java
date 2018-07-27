package com.ngc.seaside.gradle.plugins.eclipse;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.util.OsgiResolver;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base plugin for eclipse. This plugin creates two tasks, {@link #DOWNLOAD_ECLIPSE_TASK_NAME one} to download an
 * eclipse distribution to a cached directory, and {@link #UNZIP_ECLIPSE_TASK_NAME another} to unzip it. These tasks are
 * not executed by default. This plugin requires the {@link BaseEclipseExtension#setLinuxVersion(String) eclipse
 * version} and the {@link BaseEclipseExtension#setLinuxDownloadUrl(String) download url} to be set in the
 * {@link BaseEclipseExtension extension}.
 * 
 * <p>
 * Example:
 * <pre>
 * apply plugin: 'com.ngc.seaside.eclipse.updatesite'
 * eclipseDistribution {
 *    linuxVersion = 'eclipse-dsl-photon-R-linux-gtk-x86_64'
 *    windowsVersion = 'eclipse-dsl-photon-R-win32-x86_64'
 *    linuxDownloadUrl = ...
 *    windowsDownloadUrl = ...
 *    enablePluginsRepository() // creates a directory repository pointing to the downloaded eclipse distribution
 * }
 * </pre>
 * 
 * @see BaseEclipseExtension
 */
public class BaseEclipsePlugin extends AbstractProjectPlugin {

   /**
    * The base eclipse extension name.
    */
   public static final String EXTENSION_NAME = "eclipseDistribution";

   /**
    * The eclipse task group name.
    */
   public static final String ECLIPSE_TASK_GROUP_NAME = "Eclipse";

   /**
    * The name of the task for downloading the eclipse SDK.
    */
   public static final String DOWNLOAD_ECLIPSE_TASK_NAME = "downloadEclipse";

   /**
    * The name of the task for unzipping the eclipse SDK.
    */
   public static final String UNZIP_ECLIPSE_TASK_NAME = "unzipEclipse";

   private static final Pattern FILE_NAME_REGEX = Pattern.compile("(?<name>\\w+?(?:\\.\\w+?)*?)[\\.\\-_]"
            + "(?<version>\\d+?(?:\\.\\d+?)*?(?:[\\.\\-_]\\w+?)?)\\.(?<extension>jar)");
   private static final Pattern VERSION_REGEX = Pattern.compile("\\d+?(?:\\.\\d+?)*?(?:[\\-_]\\w+?)?");

   @Override
   protected void doApply(Project project) {
      BaseEclipseExtension extension =
               project.getExtensions().create(EXTENSION_NAME, BaseEclipseExtension.class, project);

      TaskContainer tasks = project.getTasks();
      DownloadEclipseTask downloadTask = tasks.create(DOWNLOAD_ECLIPSE_TASK_NAME, DownloadEclipseTask.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Downloads the Eclipse SDK");
         task.getEclipseArchive().set(extension.getArchive());
         task.getEclipseDownloadUrl().set(extension.getDownloadUrl());
      });

      tasks.create(UNZIP_ECLIPSE_TASK_NAME, UnzipEclipseTask.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Unzips the Eclipse SDK");
         task.dependsOn(downloadTask);
         task.getEclipseArchive().set(downloadTask.getEclipseArchive());
         task.getUnzippedDistributionDirectory().set(extension.getDistributionDirectory());
      });

      project.afterEvaluate(__ -> {
         if (extension.isPluginsRepository()) {
            project.getRepositories().flatDir(repo -> {
               repo.setName("eclipseRepository");
               repo.dirs(extension.getPluginsDirectory());
            });
         }
      });
   }

   /**
    * Returns a valid eclipse file name for the given OSGi bundle jar, or {@link Optional#empty()} if the file is not a
    * valid OSGi bundle.
    * 
    * @param file OSGi bundle jar
    * @return a valid eclipse file name
    */
   public static Optional<String> getValidEclipseName(File file) {
      Optional<String> symbolicName = OsgiResolver.getOsgiSymbolicName(file);
      if (symbolicName.isPresent()) {
         String version = OsgiResolver.getOsgiVersion(file).get();
         String name = symbolicName.get() + "_" + version + "."
                  + FilenameUtils.getExtension(file.getName());
         return Optional.of(name);
      }
      return Optional.empty();
   }

   /**
    * Returns a valid eclipse file name for the given file name, or {@link Optional#empty()} if a valid file name cannot
    * be created.
    * 
    * @param fileName file name
    * @return a valid eclipse file name
    */
   public static Optional<String> getValidEclipseName(String fileName) {
      Matcher m = FILE_NAME_REGEX.matcher(fileName);
      if (m.matches()) {
         String name = m.group("name");
         String version = m.group("version").replace('-', '.').replace('_', '.');
         String extension = m.group("extension");
         return Optional.of(name + "_" + version + "." + extension);
      }
      return Optional.empty();
   }

   /**
    * Returns a valid eclispe version for the given version, or {@link Optional#empty()} if a valid version cannot be
    * created.
    * 
    * @param version version
    * @return a valid eclipse version
    */
   public static Optional<String> getValidEclipseVersion(String version) {
      Matcher m = VERSION_REGEX.matcher(version);
      if (m.matches()) {
         return Optional.of(version.replace('-', '.').replace('_', '.'));
      }
      return Optional.empty();
   }

}
