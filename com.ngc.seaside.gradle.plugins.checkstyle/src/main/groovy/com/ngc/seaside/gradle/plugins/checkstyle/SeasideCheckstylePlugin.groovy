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
package com.ngc.seaside.gradle.plugins.checkstyle

import com.ngc.seaside.gradle.api.AbstractProjectPlugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension

/**
 * Configures the Checkstyle plugin to use the CEACIDE rule set and suppression file.
 */
class SeasideCheckstylePlugin extends AbstractProjectPlugin {

   public static final String CHECKSTYLE_TOOL_VERSION = '8.8'
   public static final String CHECKSTYLE_FAIL_ON_ERROR_PROPERTY = 'fail-on-checkstyle-error'
   public static final String CHECKSTYLE_CONFIG_FILE_NAME = 'ceacide_checks.xml'
   public static final String CHECKSTYLE_CONFIG_FILE =
         'com/ngc/seaside/gradle/plugins/checkstyle/' + CHECKSTYLE_CONFIG_FILE_NAME
   public static final String CHECKSTYLE_SUPPRESS_FILE_NAME = 'suppressions.xml'
   public static final String CHECKSTYLE_SUPPRESS_FILE =
         'com/ngc/seaside/gradle/plugins/checkstyle/' + CHECKSTYLE_SUPPRESS_FILE_NAME

   @Override
   void doApply(Project project) {
      applyPlugins(project)

      project.afterEvaluate {
         configureCheckstyleTask(project)
         configureCheckstyle(project)
      }
   }

   private static void applyPlugins(Project project) {
      project.getPlugins().apply('java')
      project.getPlugins().apply('checkstyle')
   }

   private static void configureCheckstyleTask(Project project) {
      // Ensure that checkstyleMain or checkstyleTest can only be called when running the task from the command line.
      // We don't want checkstyle to run on ever build due to time.
      project.tasks.withType(Checkstyle) { task ->
         enabled = (project.gradle.startParameter.taskNames.contains(task.name) ||
                    project.gradle.startParameter.taskNames.contains("ci"))


         if (project.gradle.startParameter.taskNames.contains(task.name)) {
            CheckstyleExtension extension = project.getExtensions().getByType(CheckstyleExtension.class)
            File tempDir = File.createTempDir()

            def resource = SeasideCheckstylePlugin.class
                  .getClassLoader()
                  .getResourceAsStream(CHECKSTYLE_CONFIG_FILE)
                  .text
            def suppressionResource = SeasideCheckstylePlugin.class
                  .getClassLoader()
                  .getResourceAsStream(CHECKSTYLE_SUPPRESS_FILE)
                  .text

            File output = new File(tempDir, CHECKSTYLE_CONFIG_FILE_NAME)
            output << resource

            File suppressionsOutput = new File(tempDir, CHECKSTYLE_SUPPRESS_FILE_NAME)
            suppressionsOutput << suppressionResource

            extension.configFile = output
            extension.configProperties = ["suppressionFile": suppressionsOutput.getAbsolutePath()]
         }
      }
   }

   private static void configureCheckstyle(Project project) {
      project.extensions.configure('checkstyle', { checkstyle ->
         // The checkstyle configuration creates a temporary directory and stores the configuration from this projects
         // resources into it for each project being built.
         checkstyle.toolVersion CHECKSTYLE_TOOL_VERSION

         //determine if the fail on error property has been set and set the appropriate
         //configuration options if it is set to 'true'
         //by default, this will not fail the build.
         if (project.hasProperty(CHECKSTYLE_FAIL_ON_ERROR_PROPERTY)) {
            if (project.findProperty(CHECKSTYLE_FAIL_ON_ERROR_PROPERTY) == "true") {
               checkstyle.ignoreFailures = false
               checkstyle.maxErrors = 0
               checkstyle.maxWarnings = 0
            }
         }
      })
   }
}
