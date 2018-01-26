package com.ngc.seaside.gradle.util

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import org.gradle.api.Project

class ReleaseUtil {

   static final String DRY_RUN_TASK_NAME_SUFFIX = 'DryRun'
   static final String RELEASE_VERSION = 'releaseVersion'
   
   private ReleaseUtil(){}
   
   static boolean isExtensionSet(Project project) {
      return project.rootProject.hasProperty(RELEASE_VERSION)
   }

   static void configureTask(Project project,
                              Class type,
                              String group,
                              String name,
                              String description,
                              SeasideReleaseExtension releaseExtension) {
      boolean isDryRun = name.endsWith(DRY_RUN_TASK_NAME_SUFFIX)
      project.afterEvaluate {
         def task = project.task(name,
                 type: type,
                 group: group,
                 description: description) {
            dryRun = isDryRun
            dependsOn {
               project.rootProject.subprojects.collect { subproject ->
                  TaskResolver.findTask(subproject, "build")
               }
            }
         }

         if (releaseExtension.uploadArtifacts && !isDryRun) {
            task.dependsOn(TaskResolver.findTask(project,"uploadArchives"))
         }
      }
   }

   static SeasideReleaseExtension getReleaseExtension(Project project, String extensionName) {
      return (SeasideReleaseExtension)project.extensions
              .findByName(extensionName)
   }
}
