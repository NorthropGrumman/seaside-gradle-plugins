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
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.util.Collections;

public class SeasideSystemDistributionPlugin extends AbstractProjectPlugin {

   public static final String DISTRIBUTION_DIRECTORY = "distribution";
   public static final String DISTRIBUTION_CONFIG_NAME = "distribution";
   public static final String ZIP_DISTRIBUTION_TASK = "createSystemDistribution";

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
         File linuxScript = extension.getScripts().getLinuxScript();
         if (linuxScript == null) {
            createLinuxScript.fromResource(
               Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("start.sh")));
         } else {
            createLinuxScript.from(linuxScript);
         }
      });

      tasks.create(ZIP_DISTRIBUTION_TASK, Zip.class, task -> {
         task.setDescription("Creates the distribution zip");
         task.dependsOn(createWindowsScript, createLinuxScript);
         tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(task);
         task.setDestinationDir(new File(project.getBuildDir(), DISTRIBUTION_DIRECTORY));
         task.setIncludeEmptyDirs(true);

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
      project.afterEvaluate(__ -> {
         if (plugins.hasPlugin(MavenPlugin.class)) {
            project.artifacts(handler -> handler.add(Dependency.ARCHIVES_CONFIGURATION, zipDistribution));
         }
         if (plugins.hasPlugin(MavenPublishPlugin.class)) {
            project.getExtensions().configure(PublishingExtension.class, convention -> {
               convention.publications(publications -> {
                  publications.create("mavenDistribution",
                     MavenPublication.class,
                     publication -> publication.artifact(zipDistribution));
               });
            });
         }
      });
   }
}
