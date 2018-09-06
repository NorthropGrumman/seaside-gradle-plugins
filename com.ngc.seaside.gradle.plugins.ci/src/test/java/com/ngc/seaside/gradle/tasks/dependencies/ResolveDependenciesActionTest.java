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

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.dsl.ScriptHandler;
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
import java.util.Collection;
import java.util.Collections;

import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newDependency;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newDependencyResult;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newLocalMavenRepo;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newNotFoundException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResolveDependenciesActionTest {

   private ResolveDependenciesAction action;

   private ProjectInternal project = GradleMocks.newProjectMock();

   private Configuration config = GradleMocks.newConfiguration("test");

   @Mock
   private PopulateMaven2Repository task;

   @Mock
   private RepositorySystem repositorySystem;

   @Mock
   private RepositorySystemSession session;

   @Rule
   public TemporaryFolder localRepositoryDirectory = new TemporaryFolder();

   @Rule
   public TemporaryFolder outputDirectory = new TemporaryFolder();

   @Before
   public void setup() {
      Logger logger = mock(Logger.class);

      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      ConfigurationContainer scriptConfigs = mock(ConfigurationContainer.class);
      ScriptHandler scriptHandler = mock(ScriptHandler.class);
      when(scriptHandler.getConfigurations()).thenReturn(scriptConfigs);
      when(project.getBuildscript()).thenReturn(scriptHandler);
      when(project.getRootProject()).thenReturn(project);

      MavenArtifactRepository local = newLocalMavenRepo(localRepositoryDirectory.getRoot());
      when(task.getOutputDirectory()).thenReturn(outputDirectory.getRoot());
      when(task.getConfigurations()).thenReturn(Collections.singletonList(config));
      when(task.getLocalRepository()).thenReturn(local);

      action = new ResolveDependenciesAction() {
         @Override
         protected RepositorySystem newRepositorySystem() {
            return repositorySystem;
         }

         @Override
         protected RepositorySystemSession newSession(RepositorySystem repositorySystem) {
            return session;
         }
      };
   }

   @Test
   public void doesResolveDependencies() throws Throwable {
      config.getDependencies().add(newDependency("a", "b", "1.0"));

      DependencyResult jarResult = newDependencyResult(new File("."));
      DependencyResult sourcesResult = newDependencyResult(new File("."));
      DependencyResult testsResult = newDependencyResult(new File("."));
      DependencyResult javadocResult = newDependencyResult(new File("."));
      when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(jarResult)
            .thenReturn(sourcesResult)
            .thenReturn(testsResult)
            .thenReturn(javadocResult);

      action.execute(task);
      Collection<DependencyResult> results = action.getDependencyResults();
      assertFalse("no results returned!",
                  results.isEmpty());
      assertTrue("missing main JAR result!",
                 results.contains(jarResult));
      assertTrue("missing sources result!",
                 results.contains(sourcesResult));
      assertTrue("missing tests result!",
                 results.contains(testsResult));
      assertTrue("missing javadoc result!",
                 results.contains(javadocResult));
      assertEquals("contains extra results!",
                   4,
                   results.size());
   }

   @Test
   public void doesSkipMissingClassifiers() throws Throwable {
      config.getDependencies().add(newDependency("a", "b", "1.0"));

      DependencyResult jarResult = newDependencyResult(new File("."));
      DependencyResult sourcesResult = newDependencyResult(new File("."));
      when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(jarResult)
            .thenReturn(sourcesResult)
            .thenThrow(newNotFoundException())
            .thenThrow(newNotFoundException());

      action.execute(task);
      Collection<DependencyResult> results = action.getDependencyResults();
      assertFalse("no results returned!",
                  results.isEmpty());
      assertTrue("missing main JAR result!",
                 results.contains(jarResult));
      assertTrue("missing sources result!",
                 results.contains(sourcesResult));
      assertEquals("contains extra results!",
                   2,
                   results.size());
   }

   @Test
   public void testDoesResolveConfigurationsIfConfigured() throws Throwable {
      config.getDependencies().add(newDependency("a", "b", "1.0"));

      DependencyResult jarResult = newDependencyResult(new File("."));
      DependencyResult sourcesResult = newDependencyResult(new File("."));
      DependencyResult testsResult = newDependencyResult(new File("."));
      DependencyResult javadocResult = newDependencyResult(new File("."));
      when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(jarResult)
            .thenReturn(sourcesResult)
            .thenReturn(testsResult)
            .thenReturn(javadocResult);

      when(config.getName()).thenReturn("config1");
      when(task.getConfigurationsToResolve()).thenReturn(Collections.singletonList("config1"));
      when(config.resolve()).thenReturn(Collections.emptySet());

      action.execute(task);

      verify(config).resolve();
   }
}
