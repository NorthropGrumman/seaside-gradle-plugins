package com.ngc.seaside.gradle.util

import com.ngc.seaside.gradle.extensions.release.SeasideReleaseExtension
import org.gradle.api.Project

/**
 * Things common to all Release Tasks
 */
class ReleaseUtil {

   static final String DRY_RUN_TASK_NAME_SUFFIX = 'DryRun'
   static final String RELEASE_VERSION = 'releaseVersion'

   /**
    * Wanted this to be strictly used as a static utility class
    */
   private ReleaseUtil(){}

   /**
    * To check for the release property in the current release project
    *
    * @param project of current release
    * @return boolean to indicate whether or not the release has been
    *     set in the current project
    */
   static boolean isExtensionSet(Project project) {
      return project.rootProject.hasProperty(RELEASE_VERSION)
   }

   /**
    *
    * A one stop place to create a release type gradle task
    *
    * @param project current gradle release
    * @param type Gradle Task that you are creating
    * @param group that this task belongs to
    * @param name of the task you are creating
    * @param description describes what the gradle task does
    * @param releaseExtension that belongs to this project
    */
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

   /**
    *
    * @param arguments
    */
   static void git(Project project, Object[] arguments) {

      def output = new ByteArrayOutputStream()

      project.exec {
         executable "git"
         args arguments
         standardOutput output
         ignoreExitValue = true
      }

      output = output.toString().trim()
      if (!output.isEmpty()) {
         project.logger.debug(output)
      }
   }
}
