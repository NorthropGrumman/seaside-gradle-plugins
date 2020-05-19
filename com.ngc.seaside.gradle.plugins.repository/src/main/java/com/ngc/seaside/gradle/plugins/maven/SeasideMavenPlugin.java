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
      project.getPlugins().withType(JavaPlugin.class, plugin -> {
         PublishingExtension extension = project.getExtensions().getByType(PublishingExtension.class);
         extension.publications(publications -> {
            publications.create(MAVEN_JAVA_PUBLICATION_NAME, MavenPublication.class, publication -> {
               publication.from(project.getComponents().getByName("java"));
            });
         });
      });
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
