package com.ngc.seaside.gradle.api.plugins;

import com.ngc.seaside.gradle.tasks.release.ReleaseType;
import com.ngc.seaside.gradle.util.TaskResolver;
import com.ngc.seaside.gradle.util.VersionResolver;

import org.gradle.api.Project;

public abstract class AbstractProjectPlugin implements IProjectPlugin {

   public static final String VERSION_SETTINGS_CONVENTION_NAME = "versionSettings";

   private VersionResolver versionResolver;
   private TaskResolver taskResolver;

   /**
    * Inject project version configuration and force subclasses to use it
    * 
    * @param project project applying this plugin
    */
   @Override
   public void apply(Project project) {
      taskResolver = new TaskResolver(project);
      versionResolver = (VersionResolver) project.getExtensions().findByName(VERSION_SETTINGS_CONVENTION_NAME);
      if (versionResolver == null) {
         versionResolver = project.getExtensions()
                                  .create(VERSION_SETTINGS_CONVENTION_NAME, VersionResolver.class, project);
      }
      versionResolver.setEnforceVersionSuffix(false);
      project.setVersion(new Object() {
         @Override
         public String toString() {
            return versionResolver.getUpdatedProjectVersionForRelease(ReleaseType.SNAPSHOT);
         }
      });
      doApply(project);
   }

   @Override
   public TaskResolver getTaskResolver() {
      return taskResolver;
   }

   @Override
   public VersionResolver getVersionResolver() {
      return versionResolver;
   }

   /**
    * Default action for applying a project plugin.
    * 
    * @param project project applying this plugin
    */
   protected abstract void doApply(Project project);

}
