package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

import org.gradle.api.InvalidUserDataException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CreateDeploymentScriptAction extends DefaultTaskAction<PopulateMaven2Repository> {

   final static String DEPLOYMENT_SCRIPT_RESOURCE_NAME = "com/ngc/seaside/gradle/tasks/dependencies/deploy.sh";

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(!task.isCreateDeploymentScriptFile() || task.getDeploymentScriptFile() != null,
                               "deployment script file is not configured!");
   }

   @Override
   protected void doExecute() {
      if (task.isCreateDeploymentScriptFile()) {
         File script = task.getDeploymentScriptFile();

         // Make the directory if necessary.
         File parentDir = script.getParentFile();
         if (parentDir != null && !parentDir.isDirectory()) {
            parentDir.mkdirs();
         }

         logger.lifecycle("Creating deployment script {}.", script);
         try (InputStream is = CreateDeploymentScriptAction.class
               .getClassLoader()
               .getResourceAsStream(DEPLOYMENT_SCRIPT_RESOURCE_NAME)) {
            // Copy the script to the output directory.
            Files.copy(is, script.toPath());
         } catch (IOException e) {
            throw new IllegalStateException("failed to load configuration properties from classpath!", e);
         }
      }
   }
}
