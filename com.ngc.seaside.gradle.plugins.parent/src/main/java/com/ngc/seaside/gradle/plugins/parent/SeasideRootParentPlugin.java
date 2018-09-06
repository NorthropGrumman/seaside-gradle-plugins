/**
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
package com.ngc.seaside.gradle.plugins.parent;

import com.hierynomus.gradle.license.tasks.LicenseCheck;
import com.hierynomus.gradle.license.tasks.LicenseFormat;
import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import nl.javadude.gradle.plugins.license.LicenseExtension;
import nl.javadude.gradle.plugins.license.LicensePlugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * A plugin that is applied to a <i>root</i> project (not sub-projects).  This plugin mostly just configures licenses
 * for all Gradle files that make up an entire project.
 */
public class SeasideRootParentPlugin extends AbstractProjectPlugin {

   /**
    * The name of the group for license tasks.
    */
   public static final String LICENSE_GROUP_NAME = "License";

   /**
    * A tasks that checks the license of all files.
    */
   public static final String LICENSE_CHECK_TASK_NAME = "license";

   /**
    * A tasks that applies the configured license to all files.
    */
   public static final String LICENSE_FORMAT_TASK_NAME = "licenseFormat";

   /**
    * A task that checks the license applied to Gradle build scripts.
    */
   public static final String LICENSE_CHECK_GRADLE_SCRIPTS_TASK_NAME = "licenseCheckGradleScripts";

   /**
    * A task that applies the configured license to Gradle build scripts.
    */
   public static final String LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME = "licenseFormatGradleScripts";

   @Override
   protected void doApply(Project project) {
      applyPlugins(project);
      configureLicense(project);
      createTasks(project);
   }

   private void applyPlugins(Project project) {
      project.getPlugins().apply(LifecycleBasePlugin.class);
      project.getPlugins().apply(LicensePlugin.class);
   }

   private void configureLicense(Project project) {
      LicenseExtension license = project.getExtensions().getByType(LicenseExtension.class);
      license.mapping("gradle", "SLASHSTAR_STYLE");
   }

   private void createTasks(Project project) {
      Task license = project.getTasks().getByName(LICENSE_CHECK_TASK_NAME);
      Task licenseFormat = project.getTasks().getByName(LICENSE_FORMAT_TASK_NAME);

      LicenseCheck checkBuildScriptsLicense = project.getTasks().create(LICENSE_CHECK_GRADLE_SCRIPTS_TASK_NAME,
                                                                        LicenseCheck.class);
      LicenseFormat formatBuildScriptsLicense = project.getTasks().create(LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME,
                                                                          LicenseFormat.class);
      checkBuildScriptsLicense.setGroup(LICENSE_GROUP_NAME);
      formatBuildScriptsLicense.setGroup(LICENSE_GROUP_NAME);
      checkBuildScriptsLicense.setDescription("Check for header consistency of Gradle build scripts.");
      formatBuildScriptsLicense.setDescription("Applies the configured license to Gradle build scripts.");
      checkBuildScriptsLicense.source(project.getLayout().getProjectDirectory())
            .include("**/*.gradle")
            .exclude("*/build/");
      formatBuildScriptsLicense.source(project.getLayout().getProjectDirectory())
            .include("**/*.gradle")
            .exclude("*/build/");

      license.dependsOn(checkBuildScriptsLicense);
      licenseFormat.dependsOn(formatBuildScriptsLicense);
   }
}
