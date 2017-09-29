package com.ngc.seaside.gradle.plugins.bats

import com.ngc.seaside.gradle.extensions.bats.SeasideBatsExtension
import com.ngc.seaside.gradle.tasks.bats.SeasideRunBatsTask
import com.ngc.seaside.gradle.tasks.bats.SeasideExtractBatsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class SeasideBatsPlugin implements Plugin<Project> {
   public static final String BATS_EXTENSION_NAME = "seasideBats"
   public static final String BATS_TASK_GROUP_NAME = "Bats"
   public static final String RUN_BATS_TASK_NAME = "runBats"
   public static final String EXTRACT_BATS_TASK_NAME = "extractBats"

   String resultsFile
   String batsTestsDir

   @Override
   void apply(Project p) {
      p.configure(p) {
         SeasideBatsExtension e = createTheBatsExtensionOnTheProject(p)
         initializeConfigurableBatsExtensionProperties(e)

         buildscript {
            configurations {
               classpath
            }
         }

         task(
            EXTRACT_BATS_TASK_NAME,
            type: SeasideExtractBatsTask,
            group: BATS_TASK_GROUP_NAME,
            description: "Extract the bats release archive",
            dependsOn: "build")

         task(
            RUN_BATS_TASK_NAME,
            type: SeasideRunBatsTask,
            group: BATS_TASK_GROUP_NAME,
            description: "Run the bats command on the specified directory",
            dependsOn: EXTRACT_BATS_TASK_NAME)
      }
   }

   private static createTheBatsExtensionOnTheProject(Project p) {
      return p.extensions
              .create(BATS_EXTENSION_NAME, SeasideBatsExtension, p)
   }

   private initializeConfigurableBatsExtensionProperties(SeasideBatsExtension e) {
      e.resultsFile = resultsFile ?: e.resultsFile
      e.batsTestsDir = batsTestsDir ?: e.batsTestsDir
   }
}
