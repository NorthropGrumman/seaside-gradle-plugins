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
   private ReleaseUtil() {}

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

   /**
    *
    * @param project for current release
    * @param extensionName that you are looking to retrieve
    * @return a SeasideReleaseExtension
    */
   static SeasideReleaseExtension getReleaseExtension(Project project, String extensionName) {
      return (SeasideReleaseExtension)project.extensions
              .findByName(extensionName)
   }

   /**
    * Execute a git command on the current Gradle project passing the specified arguments to git.
    *
    * @param arguments The arguments to pass to the git command.
    *
    * @return The closure to be added to your project for running the git command with the arguments specified.
    */
   static Closure git(Object[] arguments) {
      return gitWithOutput(null, arguments)
   }

   /**
    * Execute a git command on the current Gradle project passing the specified arguments to git and redirecting
    * standard output and standard error to the specified output stream. This could be useful for testing that the
    * command's output is what you expect.
    *
    * @param output The output stream to which the command output should be directed.
    * @param arguments The arguments to pass to the git command.
    *
    * @return The closure to be added to your project for running the git command with the arguments specified.
    */
   static Closure gitWithOutput(ByteArrayOutputStream output, Object[] arguments) {
      println("trying to run git $arguments")
      return {
         executable "git"
         args arguments
         standardOutput output ?: new ByteArrayOutputStream()
         ignoreExitValue = true
      }
   }
}
