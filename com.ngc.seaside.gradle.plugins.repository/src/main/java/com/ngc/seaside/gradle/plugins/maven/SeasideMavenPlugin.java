/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.plugins.maven;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;

/**
 * Convenience plugin for configuring the {@link MavenPublishPlugin maven-publish plugin}. This plugin will prevent
 * build scripts from also applying the {@link MavenPlugin maven plugin}. It creates install, upload, and
 * uploadArchives tasks (named from the maven plugin) that are aliases to the maven-publish plugin's counterpart tasks.
 * This plugin will also configure a {@code mavenJava} publication if the {@link JavaPlugin java plugin} is applied.
 */
public class SeasideMavenPlugin extends AbstractProjectPlugin {

   public static final String MAVEN_JAVA_PUBLICATION_NAME = "mavenJava";

   @Override
   protected void doApply(Project project) {
      configurePlugins(project);
      configureTasks(project);
   }

   private void configurePlugins(Project project) {
      project.getPlugins().apply(MavenPublishPlugin.class);
      project.getPlugins().withType(MavenPlugin.class, plugin -> {
         throw new GradleException("maven plugin cannot be applied with the Seaside Maven Plugin");
      });
      // Do not do this logic if the MavenPublishPluginPlugin is applied.  This plugin is applied by the
      // java-gradle-plugin.  If we don't do this, multiple publications with the same GAVs are configured which
      // results in issues during uploads of releases.
      if (!project.getPlugins().hasPlugin(JavaGradlePluginPlugin.class)) {
         project.getPlugins().withType(JavaPlugin.class, plugin -> {
            PublishingExtension extension = project.getExtensions().getByType(PublishingExtension.class);
            extension.publications(publications -> {
               publications.create(MAVEN_JAVA_PUBLICATION_NAME, MavenPublication.class, publication -> {
                  publication.from(project.getComponents().getByName("java"));
               });
            });
         });
      }
   }

   private void configureTasks(Project project) {
      TaskContainer tasks = project.getTasks();
      tasks.register("install", task -> {
         task.dependsOn(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME);
         task.setDescription("Alias for " + MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME);
         task.doFirst(__ -> task.getLogger().warn("install task is deprecated; use "
                                                        + MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME
                                                        + " task instead"));
      });
      tasks.register("upload", task -> {
         task.dependsOn(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME);
         task.setDescription("Alias for " + PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME);
         task.doFirst(__ -> task.getLogger().warn(task.getName() + " task is deprecated; use "
                                                        + PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME
                                                        + " task instead"));
      });
      tasks.register("uploadArchives", task -> {
         task.dependsOn(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME);
         task.setDescription("Alias for " + PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME);
         task.doFirst(__ -> task.getLogger().warn(task.getName() + " task is deprecated; use "
                                                        + PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME
                                                        + " task instead"));
      });
   }

}
