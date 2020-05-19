/**
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
 * for all Gradle files that make up an entire project and applies the {@link LicensePlugin} to all subprojects.
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
      configureSubprojects(project);
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

   private void configureSubprojects(Project project) {
      project.getSubprojects().forEach(subproject -> subproject.getPlugins().apply(LicensePlugin.class));
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
