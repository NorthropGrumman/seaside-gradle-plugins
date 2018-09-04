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
package com.ngc.seaside.gradle.tasks.dependencies;

import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newArtifactResult;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newLocalMavenRepo;
import static com.ngc.seaside.gradle.tasks.dependencies.CreateDependencyReportAction.formatLine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CreateDependencyReportActionIT {

   private CreateDependencyReportAction action;

   private File reportFile;

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
      reportFile = new File(temp.getRoot(), "dependencies.csv");
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
      action = new CreateDependencyReportAction();
   }

   @Test
   public void testDoesGenerateReportFile() throws Throwable {
      when(task.isCreateDependencyReportFile()).thenReturn(true);
      when(task.getDependencyInfoReportFile()).thenReturn(reportFile);
      action.setStore(store.addResult(jar, pom).addResult(sources, pom).addResult(tests, pom).finish());

      action.execute(task);

      assertTrue("did not create CSV file!",
                 reportFile.exists());
      List<String> lines = Files.readAllLines(reportFile.toPath());
      lines.forEach(System.out::println);
      assertTrue("missing jar dependency from report!",
                 lines.contains(formatLine(jar, store, reportFile.toPath())));
   }

   @Test
   public void testDoesAppendToExistingReportFile() throws Throwable {
      when(task.isCreateDependencyReportFile()).thenReturn(true);
      when(task.getDependencyInfoReportFile()).thenReturn(reportFile);
      action.setStore(store.addResult(jar, pom).addResult(sources, pom).finish());

      // Create an existing file.
      String extraLine = "group2\tartifact2\t2.0\tmy-pom.pom\tmy-file.jar\tmy-sources.jar\tsources\tjar";
      Files.write(reportFile.toPath(), Arrays.asList(CreateDependencyReportAction.COLUMN_HEADERS,
                                                     extraLine));

      action.execute(task);

      assertTrue("did not create report file!",
                 reportFile.exists());
      List<String> lines = Files.readAllLines(reportFile.toPath());
      assertTrue("missing jar dependency from report!",
                 lines.contains(formatLine(jar, store, reportFile.toPath())));
      assertTrue("did not keep old line!",
                 lines.contains(extraLine));
   }

   @Test
   public void testDoesNotInsertDuplicatesIntoReportFile() throws Throwable {
      when(task.isCreateDependencyReportFile()).thenReturn(true);
      when(task.getDependencyInfoReportFile()).thenReturn(reportFile);
      action.setStore(store.addResult(jar, pom).addResult(sources, pom).addResult(tests, pom).finish());

      // Create an existing file.
      String line = formatLine(jar, store, reportFile.toPath());
      Files.write(reportFile.toPath(), Arrays.asList(CreateDependencyReportAction.COLUMN_HEADERS,
                                                     line));

      action.execute(task);

      assertTrue("did not create dependency report file!",
                 reportFile.exists());
      List<String> lines = Files.readAllLines(reportFile.toPath());
      assertTrue("missing jar dependency from report!",
                 lines.contains(line));

      assertEquals("report contains duplicate lines!",
                   1,
                   lines.stream()
                         .filter(l -> l.equals(line))
                         .count());
   }

   @Test
   public void testDoesNotGenerateReportFileIfNotConfigured() throws Throwable {
      when(task.isCreateDependencyReportFile()).thenReturn(false);
      action.setStore(store.addResult(jar, pom));
      action.execute(task);
      assertFalse("should not have created file!",
                  reportFile.exists());
   }

   @Test
   public void testDoesValidate() throws Throwable {
      when(task.isCreateDependencyReportFile()).thenReturn(true);
      when(task.getDependencyInfoReportFile()).thenReturn(null);

      try {
         action.validate(task);
         fail("did not validate missing report file!");
      } catch (InvalidUserDataException e) {
         // Expected.
      }

      when(task.isCreateDependencyReportFile()).thenReturn(false);
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
