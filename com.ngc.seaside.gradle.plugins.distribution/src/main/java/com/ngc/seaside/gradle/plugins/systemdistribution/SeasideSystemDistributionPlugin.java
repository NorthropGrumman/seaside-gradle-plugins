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
package com.ngc.seaside.gradle.plugins.systemdistribution;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.distribution.ResourceCopyTask;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.Collections;

/**
 * Plugin for creating a distribution composed of sub-distributions.
 *
 * <p>
 * This plugin provides the
 * {@value com.ngc.seaside.gradle.plugins.systemdistribution.SeasideSystemDistributionExtension#NAME} extension
 * of type {@link SeasideSystemDistributionExtension}.
 *
 * <p>
 * When built, this plugin will produce a distribution zip. The zip will contain start scripts and the
 * specified sub-distributions.
 *
 * <p>
 * This plugin provides the {@value #DISTRIBUTION_CONFIG_NAME} configuration. Dependencies added to this configuration
 * must reference zip-formatted files. The default start scripts for this plugin's distribution assume that these
 * sub-distributions have start scripts in their {@code bin/} folder. The default start scripts can be overridden using
 * the extension.
 *
 * <p>
 * Example:
 *
 * <pre>
 * apply plugin: 'com.ngc.seaside.repository'
 * apply plugin: 'com.ngc.seaside.distribution.system.distribution'
 *
 * ext {
 *    threatEvalVersion = '2.4.0'
 * }
 *
 * dependencies {
 *    distribution "com.ngc.seaside.threateval:etps.distribution:$threatEvalVersion@zip"
 *    distribution "com.ngc.seaside.threateval:ctps.distribution:$threatEvalVersion@zip"
 *    distribution "com.ngc.seaside.threateval:datps.distribution:$threatEvalVersion@zip"
 *    distribution "com.ngc.seaside.threateval:tps.distribution:$threatEvalVersion@zip"
 * }
 * </pre>
 */
public class SeasideSystemDistributionPlugin extends AbstractProjectPlugin {

   public static final String DISTRIBUTION_DIRECTORY = "distribution";
   public static final String DISTRIBUTION_CONFIG_NAME = "distribution";
   public static final String ZIP_DISTRIBUTION_TASK = "createSystemDistribution";
   public static final String RESOURCES_DIRECTORY = "src/main/resources";

   /**
    * Used to set executable permissions on the eclipse executable.  Equivalent to unix file permissions: rwxr-xr-x.
    */
   private static final int UNIX_EXECUTABLE_PERMISSIONS = 493;

   @Override
   protected void doApply(Project project) {
      applyPlugins(project);
      createExtension(project);
      createConfigurations(project);
      configureTasks(project);
      createArchives(project);
   }

   private void applyPlugins(Project project) {
      project.getPlugins().apply(BasePlugin.class);
   }

   private void createExtension(Project project) {
      project.getExtensions().create(SeasideSystemDistributionExtension.NAME,
                                     SeasideSystemDistributionExtension.class,
                                     project);
   }

   private void createConfigurations(Project project) {
      project.getConfigurations().create(DISTRIBUTION_CONFIG_NAME);
   }

   private void configureTasks(Project project) {
      TaskContainer tasks = project.getTasks();
      SeasideSystemDistributionExtension extension = project.getExtensions().findByType(
            SeasideSystemDistributionExtension.class);
      Configuration distConfig = project.getConfigurations().getByName(DISTRIBUTION_CONFIG_NAME);

      Action<ResourceCopyTask> taskAction = task -> task.setDestinationDir(task.getTemporaryDir());
      ResourceCopyTask createWindowsScript = tasks.create("createWindowsScript", ResourceCopyTask.class, taskAction);

      ResourceCopyTask createLinuxScript = tasks.create("createLinuxScript", ResourceCopyTask.class, taskAction);

      project.afterEvaluate(__ -> {
         File windowsScript = extension.getScripts().getWindowsScript();
         if (windowsScript == null) {
            createWindowsScript.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("start.bat")));
            createWindowsScript.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("stop.bat")));
         } else {
            createWindowsScript.from(windowsScript);
         }
         Action<CopySpec> copyAction = spec -> spec.eachFile(this::setExecuteBitOnShellScripts);
         File linuxScript = extension.getScripts().getLinuxScript();
         if (linuxScript == null) {
            createLinuxScript.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("start.sh")),
                  copyAction);
         } else {
            createLinuxScript.from(linuxScript, copyAction);
         }
      });

      tasks.create(ZIP_DISTRIBUTION_TASK, Zip.class, task -> {
         task.setDescription("Creates the distribution zip");
         task.dependsOn(createWindowsScript, createLinuxScript);
         tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(task);
         task.setDestinationDir(new File(project.getBuildDir(), DISTRIBUTION_DIRECTORY));
         task.setIncludeEmptyDirs(true);
         task.from(RESOURCES_DIRECTORY);

         project.afterEvaluate(__ -> {
            task.setArchiveName(extension.getDistributionName());
            task.from(createWindowsScript.getDestinationDir());
            task.from(createLinuxScript.getDestinationDir());
            distConfig.getResolvedConfiguration().getResolvedArtifacts().forEach(artifact -> {
               task.from(project.zipTree(artifact.getFile()), spec -> spec.into(distributionFolder(artifact)));
            });
         });

         // Unzip the distribution
         task.doLast(__ -> {
            project.copy(spec -> {
               String archiveName = task.getArchiveName();
               archiveName = archiveName.substring(0, archiveName.length() - task.getExtension().length() - 1);
               spec.from(project.zipTree(task.getArchivePath()));
               spec.into(project.getBuildDir() + "/" + DISTRIBUTION_DIRECTORY + "/" + archiveName);
            });
         });
      });
   }

   /**
    * Renames the bundle file to ensure that the group id is included in the filename.
    *
    * @param artifact bundle artifact
    * @return the renamed bundle filename
    */
   private String distributionFolder(ResolvedArtifact artifact) {
      StringBuilder name = new StringBuilder();
      ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
      name.append(id.getGroup()).append('.').append(id.getName()).append('-').append(id.getVersion());
      if (artifact.getClassifier() != null) {
         name.append('-').append(artifact.getClassifier());
      }
      return name.toString();
   }

   private void createArchives(Project project) {
      Task zipDistribution = project.getTasks().getByName(ZIP_DISTRIBUTION_TASK);
      PluginContainer plugins = project.getPlugins();
      project.artifacts(handler -> handler.add(Dependency.ARCHIVES_CONFIGURATION, zipDistribution));
      plugins.withType(MavenPublishPlugin.class, plugin -> {
         project.getExtensions().configure(PublishingExtension.class, convention -> {
            convention.publications(publications -> {
               publications.create("mavenDistribution",
                                   MavenPublication.class,
                                   publication -> publication.artifact(zipDistribution));
            });
         });
      });
   }

   private void setExecuteBitOnShellScripts(FileCopyDetails f) {
      if (f.getName().endsWith(".sh")) {
         f.setMode(UNIX_EXECUTABLE_PERMISSIONS);
      }
   }
}
