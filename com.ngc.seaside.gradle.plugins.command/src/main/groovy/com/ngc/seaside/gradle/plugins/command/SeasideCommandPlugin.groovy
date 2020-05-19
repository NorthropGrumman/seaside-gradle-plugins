/*
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
package com.ngc.seaside.gradle.plugins.command

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import com.ngc.seaside.gradle.plugins.maven.SeasideMavenPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * The command plugin provides common task for building ICommand projects most notably is the
 * template task that will zip the contents of src/main/template and add it to the archives to be used
 * in the maven repository.
 */
class SeasideCommandPlugin extends AbstractProjectPlugin {

   @Override
   void doApply(Project project) {
      project.configure(project) {
         /**
          * This plugin requires the java and maven plugins
          */
         applyPlugins(project)
         createTasks(project)

         def templates = new File("$project.projectDir/src/main/templates/")
         if (templates.exists()) {
            templates.eachFile {
               def templateFile = it
               project.task("createTemplate${templateFile.name}", type: Zip,
                            dependsOn: taskResolver.findTask("classes")) {
                  classifier = "template-${templateFile.name}"
                  from templateFile
                  include "*"
                  include "*/**"
                  archiveName "${project.group}.${project.name}-${project.version}-template-${templateFile.name}.zip"
                  destinationDir(new File("$project.projectDir/build/libs"))
               }
            }
         }

         project.afterEvaluate {
            configurations {
               commandTemplate
            }

            if (file("$project.projectDir/src/main/template/").exists()) {
               artifacts {
                  archives taskResolver.findTask("createTemplate")
                  commandTemplate taskResolver.findTask("createTemplate")
               }

               project.getPlugins().withType(SeasideMavenPlugin) {
                  def publishing = project.getExtensions().getByType(PublishingExtension)
                  def pub = (MavenPublication) publishing.publications
                        .getByName(SeasideMavenPlugin.MAVEN_JAVA_PUBLICATION_NAME)
                  pub.artifact(taskResolver.findTask("createTemplate"))
               }

               getOptionalTasks(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME,
                                PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                     .forEach({ task -> task.dependsOn(taskResolver.findTask("createTemplate")) })
               taskResolver.findTask(LifecycleBasePlugin.BUILD_TASK_NAME)
                     .dependsOn(taskResolver.findTask("createTemplate"))
            }

            if (templates.exists()) {
               templates.eachFile {
                  def name = it.name
                  artifacts {
                     archives project["createTemplate${name}"]
                     commandTemplate project["createTemplate${name}"]
                  }

                  project.getPlugins().withType(SeasideMavenPlugin) {
                     def publishing = project.getExtensions().getByType(PublishingExtension)
                     def pub = (MavenPublication) publishing.publications
                           .getByName(SeasideMavenPlugin.MAVEN_JAVA_PUBLICATION_NAME)
                     pub.artifact(project["createTemplate${name}"])
                  }

                  getOptionalTasks(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME,
                                   PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
                        .forEach({ task -> task.dependsOn(project["createTemplate${name}"]) })
                  taskResolver.findTask(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn project["createTemplate${name}"]
               }
            }
         }

         project.defaultTasks = [LifecycleBasePlugin.BUILD_TASK_NAME]
      }
   }

   /**
    * Create project tasks for this plugin
    * @param project
    */
   private void createTasks(Project project) {
      project.logger.info(String.format("Creating tasks for %s", project.name))
      /**
       * Create a task for generating the template zip. This will also be uploaded to Nexus.
       */
      project.task('createTemplate', type: Zip, dependsOn: taskResolver.findTask("classes")) {
         classifier = 'template'
         from "$project.projectDir/src/main/template/"
         include "*"
         include "*/**"
         archiveName "${project.group}.${project.name}-${project.version}-template.zip"
         destinationDir(new File("$project.projectDir/build/libs"))
      }
   }

   private Collection<Task> getOptionalTasks(String... names) {
      def tasks = new ArrayList<>()
      for (def name : names) {
         def task = taskResolver.findOptionalTask(name)
         if (task != null) {
            tasks.add(task)
         }
      }
      return tasks
   }

   /**
    * Applies additional plugins to the project the project
    * @param project
    */
   private static void applyPlugins(Project project) {
      project.logger.info(String.format("Applying plugins for %s", project.name))
      project.getPlugins().apply('java')
   }
}
