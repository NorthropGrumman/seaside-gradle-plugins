package com.ngc.seaside.gradle.tasks.dependencies;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateDeploymentScriptActionIT {

   private CreateDeploymentScriptAction action;

   private File scriptFile;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private PopulateMaven2Repository task;

   @Rule
   public TemporaryFolder temp = new TemporaryFolder();

   @Before
   public void setup() throws Throwable {
      scriptFile = new File(temp.getRoot(), "deploy.sh");

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
      assertEquals(
            "script file not correct!",
            Files.readAllLines(scriptFile.toPath()),
            IOUtils.readLines(CreateDeploymentScriptActionIT.class
                                    .getClassLoader()
                                    .getResourceAsStream(CreateDeploymentScriptAction.DEPLOYMENT_SCRIPT_RESOURCE_NAME),
                              StandardCharsets.US_ASCII));
   }

   @Test
   public void doesNotCreateScriptIfNotConfigured() throws Throwable {
      when(task.isCreateDeploymentScriptFile()).thenReturn(false);

      action.execute(task);
      assertFalse("script file should not be created!",
                  scriptFile.exists());
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
