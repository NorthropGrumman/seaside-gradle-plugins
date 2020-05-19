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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.apache.commons.io.IOUtils;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateDeploymentScriptActionIT {

   private CreateDeploymentScriptAction action;

   private File scriptFile;

   private File settingsFile;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private PopulateMaven2Repository task;

   @Rule
   public TemporaryFolder temp = new TemporaryFolder();

   @Before
   public void setup() throws Throwable {
      scriptFile = new File(temp.getRoot(), "deploy.sh");
      settingsFile = new File(temp.getRoot(), CreateDeploymentScriptAction.SETTINGS_FILE_NAME);

      Logger logger = mock(Logger.class);
      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      action = new CreateDeploymentScriptAction();
   }

   @Test
   public void doesCreateScript() throws Throwable {
      when(task.isCreateDeploymentScriptFile()).thenReturn(true);
      when(task.getDeploymentScriptFile()).thenReturn(scriptFile);

      action.execute(task);
      assertTrue("script file should be created!",
                 scriptFile.exists());
      assertTrue("settings file should be created!",
                 settingsFile.exists());
      assertEquals(
            "script file not correct!",
            Files.readAllLines(scriptFile.toPath()),
            IOUtils.readLines(CreateDeploymentScriptActionIT.class
                                    .getClassLoader()
                                    .getResourceAsStream(CreateDeploymentScriptAction.DEPLOYMENT_SCRIPT_RESOURCE_NAME),
                              StandardCharsets.US_ASCII));

      assertEquals(
            "settings file not correct!",
            Files.readAllLines(settingsFile.toPath()),
            IOUtils.readLines(
                  CreateDeploymentScriptActionIT.class
                        .getClassLoader()
                        .getResourceAsStream(CreateDeploymentScriptAction.DEPLOYMNENT_SETTINGS_RESOURCE_NAME),
                  StandardCharsets.US_ASCII));
   }

   @Test
   public void doesNotCreateScriptIfNotConfigured() throws Throwable {
      when(task.isCreateDeploymentScriptFile()).thenReturn(false);

      action.execute(task);
      assertFalse("script file should not be created!",
                  scriptFile.exists());
      assertFalse("settings file should not be created!",
                  settingsFile.exists());
   }

   @Test
   public void doesValidate() throws Throwable {
      when(task.isCreateDeploymentScriptFile()).thenReturn(true);
      when(task.getDeploymentScriptFile()).thenReturn(null);

      try {
         action.validate(task);
         fail("did not validate missing script file!");
      } catch (InvalidUserDataException e) {
         // Expected.
      }

      when(task.isCreateDeploymentScriptFile()).thenReturn(false);
      // Missing file should be okay.
      action.validate(task);
   }
}
