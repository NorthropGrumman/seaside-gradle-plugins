package com.ngc.seaside.gradle.plugins.eclipse.p2;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipseExtension;
import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin;
import com.ngc.seaside.gradle.plugins.eclipse.updatesite.SeasideEclipseUpdateSitePlugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

/**
 * Plugin used for accessing remote p2 repositories.
 * 
 * <p>
 * This plugin creates the {@value #EXTENSION_NAME} extension that uses the {@link SeasideEclipseP2Extension}. Projects
 * that use this plugin must set the eclipse version and download url using the base eclipse plugin
 * {@link BaseEclipsePlugin#EXTENSION_NAME extension}.
 * 
 * <p>
 * This plugin can access a remote p2 repository using
 * {@link SeasideEclipseP2Extension#remoteRepository(String, Action)}. Calling this method will create a task that will
 * create a cached p2 repository locally and will execute any actions provided to the methods
 * {@link ExternalP2Repository#features(Action)} and {@link ExternalP2Repository#plugins(Action)} for all the features
 * and plugins found in the p2 repository.
 * Example:
 * 
 * <pre>
 * apply plugin: 'com.ngc.seaside.eclipse.updatesite'
 * apply plugin: 'com.ngc.seaside.eclipse.p2'
 * eclipseDistribution {
 *    linuxVersion = 'eclipse-dsl-photon-R-linux-gtk-x86_64'
 *    windowsVersion = 'eclipse-dsl-photon-R-win32-x86_64'
 *    linuxDownloadUrl = ...
 *    windowsDownloadUrl = ...
 * }
 * p2.remoteRepository('http://example.com/updatesite') {
 *    // Include all the external site plugins
 *    plugins { externalPlugin -&gt;
 *       dependencies {
 *          plugin externalPlugin.dependency
 *       }
 *    }
 *    // Include all the external site features
 *    features { externalFeature -&gt;
 *       eclipseUpdateSite.feature externalFeature
 *    }
 * }
 * </pre>
 * 
 * @see BaseEclipsePlugin
 * @see SeasideEclipseUpdateSitePlugin
 * @see SeasideEclipseP2Extension
 */
public class SeasideEclipseP2Plugin extends AbstractProjectPlugin {

   /**
    * The eclipse updatesite extension name.
    */
   public static final String EXTENSION_NAME = "p2";

   /**
    * The prefix name of the tasks mirroring external p2 repositories.
    */
   public static final String MIRROR_P2_REPOSITORY_TASK_PREFIX = "mirrorP2";

   @Override
   protected void doApply(Project project) {
      project.getPlugins().apply(BaseEclipsePlugin.class);
      project.getExtensions().create(EXTENSION_NAME, SeasideEclipseP2Extension.class, project);
      createTasks(project);
   }

   private void createTasks(Project project) {
      SeasideEclipseP2Extension extension =
               (SeasideEclipseP2Extension) project.getExtensions().getByName(EXTENSION_NAME);
      BaseEclipseExtension baseExtension =
               (BaseEclipseExtension) project.getExtensions().getByName(BaseEclipsePlugin.EXTENSION_NAME);
      TaskContainer tasks = project.getTasks();

      project.afterEvaluate(__ -> {
         for (ExternalP2Repository site : extension.getExternalUpdateSites()) {
            String mirrorName = MIRROR_P2_REPOSITORY_TASK_PREFIX + site.getCleanUrl();
            tasks.create(mirrorName, MirrorP2Task.class, task -> {
               task.dependsOn(BaseEclipsePlugin.UNZIP_ECLIPSE_TASK_NAME);
               task.getEclipseExecutable().set(baseExtension.getExecutable());
               task.getP2Directory().set(site.getCacheMirrorSiteDirectory());
               task.getP2Url().set(site.getUrl());
               task.doLast(___ -> site.configure());
            });
         }
      });
   }

}
