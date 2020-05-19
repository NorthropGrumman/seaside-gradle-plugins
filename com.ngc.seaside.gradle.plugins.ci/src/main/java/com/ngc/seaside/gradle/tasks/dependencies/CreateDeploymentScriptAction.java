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
package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CreateDeploymentScriptAction extends DefaultTaskAction<PopulateMaven2Repository> {

   final static String DEPLOYMENT_SCRIPT_RESOURCE_NAME = "com/ngc/seaside/gradle/plugins/ci/deploy.sh";
   final static String DEPLOYMNENT_SETTINGS_RESOURCE_NAME = "com/ngc/seaside/gradle/plugins/ci/settings.xml";
   final static String SETTINGS_FILE_NAME = "settings.xml";

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateDeploymentScriptFile() || task.getDeploymentScriptFile() != null,
                               "deployment script file is not configured!");
   }

   @Override
   protected void doExecute() {
      if (task.isCreateDeploymentScriptFile()) {
         copyFile(DEPLOYMENT_SCRIPT_RESOURCE_NAME, task.getDeploymentScriptFile());
         copyFile(DEPLOYMNENT_SETTINGS_RESOURCE_NAME, new File(task.getDeploymentScriptFile().getParentFile(),
                                                               SETTINGS_FILE_NAME));
      }
   }

   private void copyFile(String resource, File dest) {
      if (!dest.exists()) {
         // Make the directory if necessary.
         File parentDir = dest.getParentFile();
         if (parentDir != null && !parentDir.isDirectory()) {
            parentDir.mkdirs();
         }

         logger.lifecycle("Creating {} file for deployment.", dest);
         try (InputStream is = CreateDeploymentScriptAction.class
               .getClassLoader()
               .getResourceAsStream(resource)) {
            // Copy the file to the output directory.
            Files.copy(is, dest.toPath());
         } catch (IOException e) {
            throw new IllegalStateException("failed to read " + resource + " resource from classpath!", e);
         }
      }
   }
}
