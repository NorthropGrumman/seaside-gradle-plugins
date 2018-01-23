package com.ngc.seaside.gradle.tasks.dependencies

import com.ngc.seaside.gradle.util.test.GradleMocks
import com.ngc.seaside.gradle.util.test.TaskBuilder
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.resolution.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.BaseRepositoryFactory
import org.gradle.api.internal.project.ProjectInternal
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class PopulateMaven2RepositoryIT {

    private PopulateMaven2Repository task

    private ProjectInternal project = GradleMocks.newProjectMock()

    private Configuration config = GradleMocks.newConfiguration("test")

    @Mock
    private RepositorySystem repositorySystem

    @Mock
    private RepositorySystemSession session

    @Mock
    private BaseRepositoryFactory repositoryFactory

    @Rule
    public TemporaryFolder localRepositoryDirectory = new TemporaryFolder()

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder()

    private File jar

    private File sources

    private File tests

    private File pom

    @Before
    void setup() {
        // Setup some files in the fake repo.
        File dir = localRepositoryDirectory.newFolder("a", "b", "1.0")
        jar = new File(dir, "b-1.0.jar")
        sources = new File(dir, "b-1.0-sources.jar")
        tests = new File(dir, "b-1.0-tests.jar")
        pom = new File(dir, "b-1.0.pom")
        jar.createNewFile()
        sources.createNewFile()
        tests.createNewFile()
        pom.createNewFile()

        task = new TaskBuilder<PopulateMaven2Repository>(PopulateMaven2Repository)
              .setSupplier({ new MockReturningTask(repositoryFactory) })
              .setProject(project)
              .create()
    }

    @Test
    void doesResolveAndCopyDependencies() {
        config.getDependencies().add(newDependency("a", "b", "1.0"))

        DependencyResult jarResult = newDependencyResult(jar)
        DependencyResult sourcesResult = newDependencyResult(sources)
        DependencyResult testsResult = newDependencyResult(tests)
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest)))
              .thenReturn(jarResult)
              .thenReturn(sourcesResult)
              .thenReturn(testsResult)

        task.setOutputDirectory(outputDirectory.getRoot())
        task.setConfiguration(config)
        task.setLocalRepository(newLocalMavenRepo(localRepositoryDirectory.getRoot()))
        task.populateRepository()

        assertTrue("did not copy JAR file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists())
        assertTrue("did not copy sources file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-sources.jar").exists())
        assertTrue("did not copy tests file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-tests.jar").exists())
        assertTrue("did not copy POM file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.pom").exists())
    }

    @Test
    void doesSkipMissingSourcesAndTests() {
        config.getDependencies().add(newDependency("a", "b", "1.0"))

        DependencyResult jarResult = newDependencyResult(jar)
        DependencyResult sourcesResult = newDependencyResult(sources)
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest)))
              .thenReturn(jarResult)
              .thenReturn(sourcesResult)
              .thenThrow(newNotFoundException())

        task.setOutputDirectory(outputDirectory.getRoot())
        task.setConfiguration(config)
        task.setLocalRepository(newLocalMavenRepo(localRepositoryDirectory.getRoot()))
        task.populateRepository()

        assertTrue("did not copy JAR file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists())
        assertTrue("did not copy sources file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-sources.jar").exists())
        assertFalse("should not have copied tests file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-tests.jar").exists())
        assertTrue("did not copy POM file!",
                   new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.pom").exists())
    }

    @Test
    void doesNotCopyFilesIfDisabled() {
        config.getDependencies().add(newDependency("a", "b", "1.0"))

        DependencyResult jarResult = newDependencyResult(jar)
        DependencyResult sourcesResult = newDependencyResult(sources)
        DependencyResult testsResult = newDependencyResult(tests)
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest)))
              .thenReturn(jarResult)
              .thenReturn(sourcesResult)
              .thenReturn(testsResult)

        task.setConfiguration(config)
        task.setLocalRepository(newLocalMavenRepo(localRepositoryDirectory.getRoot()))
        task.setPopulateLocalRepoOnly(true)
        task.populateRepository()

        assertFalse("should not have copied JAR file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists())
        assertFalse("should not have copied sources file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-sources.jar").exists())
        assertFalse("should not have copied tests file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-tests.jar").exists())
        assertFalse("should not have copied POM file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.pom").exists())
    }

    @Test
    void doesNotCopyFilesWithRelativePaths() {
        config.getDependencies().add(newDependency("a", "b", "1.0"))

        DependencyResult jarResult = newDependencyResult(new File(localRepositoryDirectory.getRoot(), ".."))
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest)))
              .thenReturn(jarResult)
              .thenThrow(newNotFoundException())
              .thenThrow(newNotFoundException())

        task.setOutputDirectory(outputDirectory.getRoot())
        task.setConfiguration(config)
        task.setLocalRepository(newLocalMavenRepo(localRepositoryDirectory.getRoot()))
        task.populateRepository()

        assertFalse("should not copy JAR file!",
                    new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists())
    }

    private static Dependency newDependency(String group, String artifact, String version) {
        Dependency dependency = mock(Dependency)
        when(dependency.getGroup()).thenReturn(group)
        when(dependency.getName()).thenReturn(artifact)
        when(dependency.getVersion()).thenReturn(version)
        return dependency
    }

    private static MavenArtifactRepository newLocalMavenRepo(File directory) {
        MavenArtifactRepository repo = mock(MavenArtifactRepository)
        when(repo.getUrl()).thenReturn(directory.toURI())
        return repo
    }

    private static DependencyResult newDependencyResult(File file) {
        Artifact artifact = mock(Artifact)
        when(artifact.getFile()).thenReturn(file)

        ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest())
        artifactResult.setArtifact(artifact)

        DependencyResult dependencyResult = new DependencyResult(new DependencyRequest())
        dependencyResult.setArtifactResults(Collections.singletonList(artifactResult))
        return dependencyResult
    }

    private static DependencyResolutionException newNotFoundException() {
        ArtifactResolutionException cause = new ArtifactResolutionException(Collections.emptyList())
        return new DependencyResolutionException(null, cause)
    }

    class MockReturningTask extends PopulateMaven2Repository {

        MockReturningTask(BaseRepositoryFactory baseRepositoryFactory) {
            super(baseRepositoryFactory)
        }

        @Override
        protected RepositorySystem newRepositorySystem() {
            return repositorySystem
        }

        @Override
        protected RepositorySystemSession newSession(RepositorySystem repositorySystem) {
            return session
        }

    }
}
