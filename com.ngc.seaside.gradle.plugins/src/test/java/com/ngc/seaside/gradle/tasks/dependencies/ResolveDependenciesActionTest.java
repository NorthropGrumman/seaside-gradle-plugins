package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
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
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
   public void setup() throws Throwable {
      Logger logger = mock(Logger.class);
      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      MavenArtifactRepository local = newLocalMavenRepo(localRepositoryDirectory.getRoot());
      when(task.getOutputDirectory()).thenReturn(outputDirectory.getRoot());
      when(task.getConfiguration()).thenReturn(config);
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

      DependencyResult jarResult = newDependencyResult();
      DependencyResult sourcesResult = newDependencyResult();
      DependencyResult testsResult = newDependencyResult();
      when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(jarResult)
            .thenReturn(sourcesResult)
            .thenReturn(testsResult);

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
      assertEquals("contains extra results!",
                   3,
                   results.size());
   }

   @Test
   public void doesSkipMissingSourcesAndTests() throws Throwable {
      config.getDependencies().add(newDependency("a", "b", "1.0"));

      DependencyResult jarResult = newDependencyResult();
      DependencyResult sourcesResult = newDependencyResult();
      when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(jarResult)
            .thenReturn(sourcesResult)
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

   private static Dependency newDependency(String group, String artifact, String version) {
      Dependency dependency = mock(Dependency.class);
      when(dependency.getGroup()).thenReturn(group);
      when(dependency.getName()).thenReturn(artifact);
      when(dependency.getVersion()).thenReturn(version);
      return dependency;
   }

   private static MavenArtifactRepository newLocalMavenRepo(File directory) {
      MavenArtifactRepository repo = mock(MavenArtifactRepository.class);
      when(repo.getUrl()).thenReturn(directory.toURI());
      return repo;
   }

   private static DependencyResult newDependencyResult() {
      return newDependencyResult(null);
   }

   private static DependencyResult newDependencyResult(File file) {
      Artifact artifact = mock(Artifact.class);
      when(artifact.getFile()).thenReturn(file);

      ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest());
      artifactResult.setArtifact(artifact);

      DependencyResult dependencyResult = new DependencyResult(new DependencyRequest());
      dependencyResult.setArtifactResults(Collections.singletonList(artifactResult));
      return dependencyResult;
   }

   private static DependencyResolutionException newNotFoundException() {
      ArtifactResolutionException cause = new ArtifactResolutionException(Collections.emptyList());
      return new DependencyResolutionException(null, cause);
   }
}
