/*
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
package com.ngc.seaside.gradle.plugins.release

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
    * @return boolean to indicate whether or not the release has been set in the current project
    */
   static boolean isExtensionSet(Project project) {
      return project.rootProject.hasProperty(RELEASE_VERSION)
   }

   /**
    * A one stop place to create a release type Gradle task
    *
    * @param project The current gradle project.
    * @param task The Gradle task to be created.
    * @param group The task group to which the new task will belong.
    * @param name The name of the task being created.
    * @param description Describes what the gradle task does.
    * @param releaseExtension The release extension associated with the project.
    * @param dependecies The list of dependencies strings for the task.
    */
   static void configureTask(
         Project project,
         Class taskType,
         String group,
         String name,
         String description,
         List<String> dependencies = []) {
      project.task(
            name,
            type: taskType,
            group: group,
            description: description,
            dependsOn: dependencies)
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
    * Get the SeasideReleaseExtension associated with the project.
    *
    * @param project for current release
    * @param extensionName that you are looking to retrieve
    * @return a SeasideReleaseExtension
    */
   static SeasideReleaseExtension getReleaseExtension(Project project, String extensionName) {
      return (SeasideReleaseExtension)project.extensions.findByName(extensionName)
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
      return {
         executable "git"
         args arguments
         standardOutput output ?: new ByteArrayOutputStream()
         ignoreExitValue = true
      }
   }
}