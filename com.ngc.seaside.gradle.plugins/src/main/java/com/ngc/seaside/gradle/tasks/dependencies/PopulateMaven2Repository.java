package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import groovy.lang.Closure;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class PopulateMaven2Repository extends DefaultTask {

   private final static Collection<String> DEFAULT_CLASSIFIERS = Collections.unmodifiableCollection(
         Arrays.asList(null, "sources", "tests"));

   private final static String DEFAULT_EXTENSION = "jar";

   /**
    * Maven API used to make requests for artifacts.
    */
   private RepositorySystem repositorySystem;

   /**
    * Maven API used to make requests for artifacts.
    */
   private RepositorySystemSession session;

   /**
    * The remove repositories to connect to make requests.
    */
   private final List<RemoteRepository> remoteRepositories = new ArrayList<>();

   private long totalDependenciesRequired = 0;

   private long totalDependenciesRetrieved = 0;

   /**
    * Used to create instances of {@coce MavenArtifactRepository} for ease of user configuration.  Provided by Gradle at
    * runtime.
    */
   private final BaseRepositoryFactory baseRepositoryFactory;

   /**
    * The user configured output directory to populate.
    */
   private File outputDirectory;

   /**
    * The configuration to resolve dependencies for.  If not set, dependencies for all configurations will be resolved.
    */
   private Configuration configuration;

   /**
    * The remote repository to use to download artifacts if the artifacts are not in the local repository.
    */
   private MavenArtifactRepository remoteRepository;

   /**
    * The local repository to store artifacts for future use.
    */
   private MavenArtifactRepository localRepository;

   @Inject
   public PopulateMaven2Repository(BaseRepositoryFactory baseRepositoryFactory) {
      this.baseRepositoryFactory = baseRepositoryFactory;
   }

   @TaskAction
   public void populateRepository() {
      Preconditions.checkState(outputDirectory != null, "outputDirectory is not set!");
      Preconditions.checkState(localRepository != null, "local repository not set!");
      Preconditions.checkState(remoteRepository != null || Files.isDirectory(Paths.get(localRepository.getUrl())),
                               "since local repository %s is not a directory a remote repository must be configured!",
                               localRepository.getUrl());

      repositorySystem = newRepositorySystem();
      session = newSession(repositorySystem);

      Collection<Configuration> configs = getConfigurations();
      totalDependenciesRequired = configs.stream()
            .mapToLong(c -> c.getDependencies().size())
            .sum();
      getLogger().lifecycle("{} dependencies must be resolved.", totalDependenciesRequired);

      for (Configuration config : configs) {
         getLogger().lifecycle("Resolving dependencies for configuration {}.", config.getName());
         for (Dependency dependency : config.getDependencies()) {
            resolveDependency(dependency);
         }
      }
   }

   public MavenArtifactRepository maven(Action<? super MavenArtifactRepository> action) {
      MavenArtifactRepository repo = baseRepositoryFactory.createMavenRepository();
      action.execute(repo);
      return repo;
   }

   public MavenArtifactRepository maven(Closure closure) {
      return maven(ConfigureUtil.configureUsing(closure));
   }

   public MavenArtifactRepository mavenLocal() {
      return baseRepositoryFactory.createMavenLocalRepository();
   }

   @OutputDirectory
   public File getOutputDirectory() {
      return outputDirectory;
   }

   public void setOutputDirectory(File outputDirectory) {
      this.outputDirectory = Preconditions.checkNotNull(outputDirectory, "outputDirectory may not be null!");
   }

   public Configuration getConfiguration() {
      return configuration;
   }

   public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
   }

   public MavenArtifactRepository getRemoteRepository() {
      return remoteRepository;
   }

   public void setRemoteRepository(MavenArtifactRepository remoteRepository) {
      this.remoteRepository = remoteRepository;
   }

   public MavenArtifactRepository getLocalRepository() {
      return localRepository;
   }

   public void setLocalRepository(MavenArtifactRepository localRepository) {
      this.localRepository = localRepository;
   }

   protected RepositorySystem newRepositorySystem() {
      DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
      locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
      locator.addService(TransporterFactory.class, FileTransporterFactory.class);
      locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
      return locator.getService(RepositorySystem.class);
   }

   protected RepositorySystemSession newSession(RepositorySystem repositorySystem) {
      DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

      File localMavenRepo = Paths.get(localRepository.getUrl()).toFile();
      session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(
            session,
            new LocalRepository(localMavenRepo)));

      if (remoteRepository != null) {
         RemoteRepository.Builder repoBuilder = new RemoteRepository.Builder(
               remoteRepository.getName(),
               "default",
               remoteRepository.getUrl().toString());

         if (remoteRepository.getCredentials() != null
             && remoteRepository.getCredentials().getUsername() != null
             && remoteRepository.getCredentials().getPassword() != null) {
            repoBuilder.setAuthentication(new AuthenticationBuilder()
                                                .addUsername(remoteRepository.getCredentials().getUsername())
                                                .addPassword(remoteRepository.getCredentials().getPassword())
                                                .build());
         }

         remoteRepositories.add(repoBuilder.build());
      } else {
         getLogger().lifecycle("No remote repository configured, downloads will not be attempted.");
      }

      return session;
   }

   private Collection<Configuration> getConfigurations() {
      Collection<Configuration> configs;
      if (configuration != null) {
         configs = Collections.singleton(configuration);
      } else {
         configs = getProject().getConfigurations();
      }
      return configs;
   }

   private void resolveDependency(Dependency dependency) {
      if (dependency instanceof ModuleDependency) {
         doResolveDependency((ModuleDependency) dependency);
      } else {
         doResolveDependency(dependency);
      }
      totalDependenciesRetrieved++;
   }

   private void doResolveDependency(Dependency dependency) {
      getLogger().lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
                            totalDependenciesRetrieved + 1,
                            totalDependenciesRequired,
                            dependency.getGroup(),
                            dependency.getName(),
                            dependency.getVersion());

      for (String classifier : DEFAULT_CLASSIFIERS) {
         getLocalPathTo(dependency.getGroup(),
                        dependency.getName(),
                        dependency.getVersion(),
                        classifier,
                        DEFAULT_EXTENSION);
      }
   }

   private void doResolveDependency(ModuleDependency dependency) {
      if (dependency.getArtifacts().isEmpty()) {
         doResolveDependency((Dependency) dependency);
      } else {
         getLogger().lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
                               totalDependenciesRetrieved + 1,
                               totalDependenciesRequired,
                               dependency.getGroup(),
                               dependency.getName(),
                               dependency.getVersion());
         for (DependencyArtifact artifact : dependency.getArtifacts()) {
            getLocalPathTo(dependency.getGroup(),
                           dependency.getName(),
                           dependency.getVersion(),
                           artifact.getClassifier(),
                           artifact.getExtension());
         }
      }
   }

   private Path getLocalPathTo(String groupId,
                               String artifactId,
                               String version,
                               String classifier,
                               String extension) {
      String prettyGave = String.format("%s:%s:%s%s@%s",
                                        groupId,
                                        artifactId,
                                        version,
                                        classifier == null ? "" : ":" + classifier,
                                        extension);
      String remoteLogMsg = remoteRepository == null ? "no remote repository configured, no download possible"
                                                     : "download may be required";
      getLogger().lifecycle("Retrieving '{}' and its dependencies ({}) ...", prettyGave, remoteLogMsg);

      CollectRequest request = new CollectRequest();
      Artifact baseArtifact = classifier == null
                              ? new DefaultArtifact(groupId, artifactId, extension, version)
                              : new DefaultArtifact(groupId, artifactId, classifier, extension, version);
      request.setRoot(new org.eclipse.aether.graph.Dependency(baseArtifact, null));
      request.setRepositories(remoteRepositories);

      DependencyRequest dependencyRequest = new DependencyRequest(request, null);
      try {
         DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);
         for (ArtifactResult localArtifact : result.getArtifactResults()) {
            getLogger().lifecycle("Located {}.", localArtifact.getArtifact().getFile());
         }
      } catch (DependencyResolutionException e) {
         handleResolutionException(e,
                                   groupId,
                                   artifactId,
                                   version,
                                   classifier,
                                   extension,
                                   prettyGave);
      }

      return null;
   }

   private void handleResolutionException(DependencyResolutionException e,
                                          String groupId,
                                          String artifactId,
                                          String version,
                                          String classifier,
                                          String extension,
                                          String prettyGave) {
      if (e.getCause() instanceof ArtifactResolutionException) {
         if (DEFAULT_CLASSIFIERS.contains(classifier)) {
            getLogger().lifecycle("Did not resolve '{}' but that is okay since that artifact is only {}.",
                                  prettyGave,
                                  classifier);
         } else {
            getLogger().warn("Failed to resolve '{}' (this artifact may be required by the build).", prettyGave);
         }
      } else {
         getLogger().error("Encountered unexpected error while resolving '{}'.", prettyGave, e);
      }
   }
}
