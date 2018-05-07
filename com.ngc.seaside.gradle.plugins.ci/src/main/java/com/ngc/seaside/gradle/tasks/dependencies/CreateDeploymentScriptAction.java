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
