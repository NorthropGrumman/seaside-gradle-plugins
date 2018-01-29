package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newArtifactResult;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newLocalMavenRepo;
import static com.ngc.seaside.gradle.tasks.dependencies.CreateM2DeploymentScriptAction.formatMavenDeployCommand;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateM2DeploymentScriptActionIT {

   private CreateM2DeploymentScriptAction action;

   private File script;

   private File localMavenRepo;

   private File outputDirectory;

   private ArtifactResultStore store;

   private ArtifactResult jar;

   private ArtifactResult sources;

   private ArtifactResult tests;

   private Path pom;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private PopulateMaven2Repository task;

   @Rule
   public TemporaryFolder temp = new TemporaryFolder();

   @Before
   public void setup() throws Throwable {
      script = new File(temp.getRoot(), "deploy-dependencies.sh");
      localMavenRepo = temp.newFolder("local", "m2repo");
      outputDirectory = temp.newFolder("dependencies-m2");

      jar = newArtifactResult("group",
                              "artifact",
                              "1.0",
                              null,
                              "jar",
                              new File(localMavenRepo, "group/artifact/1.0/artifact-1.0.jar"));
      sources = newArtifactResult("group",
                                  "artifact",
                                  "1.0",
                                  "sources",
                                  "jar",
                                  new File(localMavenRepo, "group/artifact/1.0/artifact-1.0-sources.jar"));
      tests = newArtifactResult("group",
                                "artifact",
                                "1.0",
                                "tests",
                                "jar",
                                new File(localMavenRepo, "group/artifact/1.0/artifact-1.0-tests.jar"));
      pom = createPomFor(jar.getArtifact());

      Logger logger = mock(Logger.class);
      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      MavenArtifactRepository local = newLocalMavenRepo(localMavenRepo);
      when(task.getLocalRepository()).thenReturn(local);

      store = new ArtifactResultStore(localMavenRepo.toPath(), outputDirectory.toPath());
      action = new CreateM2DeploymentScriptAction();
   }

   @Test
   public void testDoesCreateScript() throws Throwable {
      when(task.isCreateDeployScript()).thenReturn(true);
      when(task.getDeployScriptFile()).thenReturn(script);
      action.setStore(store.addResult(jar, pom).addResult(sources, pom).addResult(tests, pom));

      action.execute(task);

      assertTrue("did not create script!",
                 script.exists());
      List<String> lines = Files.readAllLines(script.toPath());
      assertTrue("missing command to deploy main jar, sources, and test!",
                 lines.contains(formatMavenDeployCommand(jar,
                                                         store,
                                                         script.toPath())));
   }

   @Test
   public void testDoesAppendToExistingScript() throws Throwable {
      fail("not implemented");
   }

   @Test
   public void testDoesNotInsertDuplicatesIntoScript() throws Throwable {
      fail("not implemented");
   }

   @Test
   public void testDoesNotGenerateScriptIfNotConfigured() throws Throwable {
      when(task.isCreateDeployScript()).thenReturn(false);
      action.setStore(store.addResult(jar, pom));
      action.execute(task);
      assertFalse("should not have created script!",
                  script.exists());
   }

   @Test
   public void testDoesValidate() throws Throwable {
      when(task.isCreateDeployScript()).thenReturn(true);
      when(task.getDeployScriptFile()).thenReturn(null);

      try {
         action.validate(task);
         fail("did not validate missing deploy script file!");
      } catch (InvalidUserDataException e) {
         // Expected.
      }

      when(task.isCreateDeployScript()).thenReturn(false);
      // Missing file should be okay.
      action.validate(task);
   }


   private static Path createPomFor(Artifact artifact) throws IOException {
      Path parent = artifact.getFile().getParentFile().toPath();
      Path pom = parent.resolve(artifact.getArtifactId() + "-" + artifact.getVersion() + ".pom");
      Files.createDirectories(parent);
      Files.createFile(pom);
      return pom;
   }
}
