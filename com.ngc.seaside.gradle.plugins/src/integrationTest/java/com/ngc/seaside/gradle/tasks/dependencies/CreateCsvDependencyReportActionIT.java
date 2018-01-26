package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.DependencyResult;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newDependencyResult;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newLocalMavenRepo;
import static com.ngc.seaside.gradle.tasks.dependencies.CreateCsvDependencyReportAction.formatLine;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateCsvDependencyReportActionIT {

   private CreateCsvDependencyReportAction action;

   private File csvFile;

   private File localMavenRepo;

   private DependencyResult jar;

   private DependencyResult sources;

   private DependencyResult tests;

   private Path pom;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private PopulateMaven2Repository task;

   @Rule
   public TemporaryFolder outputDirectory = new TemporaryFolder();

   @Before
   public void setup() throws Throwable {
      csvFile = new File(outputDirectory.getRoot(), "dependencies.csv");
      localMavenRepo = outputDirectory.newFolder("m2-dependencies");

      jar = newDependencyResult("group",
                                "artifact",
                                "1.0",
                                null,
                                "jar",
                                new File(localMavenRepo, "group/artifact/1.0/artifact-1.0.jar"));
      sources = newDependencyResult("group",
                                    "artifact",
                                    "1.0",
                                    "sources",
                                    "jar",
                                    new File(localMavenRepo, "group/artifact/1.0/artifact-1.0-sources.jar"));
      tests = newDependencyResult("group",
                                  "artifact",
                                  "1.0",
                                  "tests",
                                  "jar",
                                  new File(localMavenRepo, "group/artifact/1.0/artifact-1.0-tests.jar"));
      pom = createPomFor(jar.getArtifactResults().get(0).getArtifact());

      Logger logger = mock(Logger.class);
      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      MavenArtifactRepository local = newLocalMavenRepo(localMavenRepo);
      when(task.getLocalRepository()).thenReturn(local);

      action = new CreateCsvDependencyReportAction();
   }

   @Test
   public void testDoesGenerateCsvFile() throws Throwable {
      when(task.isCreateCsvFile()).thenReturn(true);
      when(task.getDependencyInfoCsvFile()).thenReturn(csvFile);
      action.setDependencyResults(Arrays.asList(jar, sources, tests));

      action.execute(task);

      assertTrue("did not create CSV file!",
                 csvFile.exists());
      List<String> lines = Files.readAllLines(csvFile.toPath());
      lines.forEach(System.out::println); // TODO TH: remove
      assertTrue("missing jar dependency from report!",
                 lines.contains(formatLine(jar.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
      assertTrue("missing sources dependency from report!",
                 lines.contains(formatLine(sources.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
      assertTrue("missing tests dependency from report!",
                 lines.contains(formatLine(tests.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
   }

   @Test
   public void testDoesAppendToExistingCsvFile() throws Throwable {
      when(task.isCreateCsvFile()).thenReturn(true);
      when(task.getDependencyInfoCsvFile()).thenReturn(csvFile);
      action.setDependencyResults(Arrays.asList(jar, sources));

      action.execute(task);

      assertTrue("did not create CSV file!",
                 csvFile.exists());
      List<String> lines = Files.readAllLines(csvFile.toPath());
      lines.forEach(System.out::println); // TODO TH: remove
      assertTrue("missing jar dependency from report!",
                 lines.contains(formatLine(jar.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
      assertTrue("missing sources dependency from report!",
                 lines.contains(formatLine(sources.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
      assertTrue("missing tests dependency from report!",
                 lines.contains(formatLine(tests.getArtifactResults().get(0).getArtifact(), pom, csvFile.toPath())));
   }

   @Test
   public void testDoesNotInsertDuplicatesIntoCsvFile() throws Throwable {
      fail("not implemented");
   }

   @Test
   public void testDoesNotGenerateCsvFileIfNotConfigured() throws Throwable {
      when(task.isCreateCsvFile()).thenReturn(false);
      action.setDependencyResults(Collections.singletonList(jar));
      action.execute(task);
      assertFalse("should not have created file!",
                  csvFile.exists());
   }

   @Test
   public void testDoesValidate() throws Throwable {
      when(task.isCreateCsvFile()).thenReturn(true);
      when(task.getDependencyInfoCsvFile()).thenReturn(null);

      try {
         action.validate(task);
         fail("did not validate missing CSV file!");
      } catch (InvalidUserDataException e) {
         // Expected.
      }

      when(task.isCreateCsvFile()).thenReturn(false);
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
