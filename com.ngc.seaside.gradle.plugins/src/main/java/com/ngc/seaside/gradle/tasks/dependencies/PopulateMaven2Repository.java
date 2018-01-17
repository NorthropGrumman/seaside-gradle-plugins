package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
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
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PopulateMaven2Repository extends DefaultTask {

   private final static Collection<String> DEFAULT_CLASSIFIERS = Collections.unmodifiableCollection(
         Arrays.asList(null, "sources", "tests"));

   private final static String DEFAULT_EXTENSION = "jar";

   private RepositorySystem repositorySystem;

   private RepositorySystemSession session;

   private final List<RemoteRepository> remoteRepositories = new ArrayList<>();

   private File outputDirectory;

   private Configuration configuration;

   private IRepositoryConfiguration repositoryConfiguration;

   @TaskAction
   public void populateRepository() {
      Preconditions.checkState(outputDirectory != null, "outputDirectory is not set!");
      Preconditions.checkState(repositoryConfiguration != null, "repositoryConfiguration not set!");

      repositorySystem = newRepositorySystem();
      session = newSession(repositorySystem);

      Collection<Configuration> configs = getConfigurations();
      for (Configuration config : configs) {
         getLogger().lifecycle("Resolving dependencies for configuration {}.", config.getName());
         for (Dependency dependency : config.getDependencies()) {
            resolveDependency(dependency, repositorySystem, session);
         }
      }
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
      File localMavenRepo = repositoryConfiguration.getLocalMavenRepository().toFile();
//      Preconditions.checkState(!localMavenRepo.isFile() || localMavenRepo.isDirectory(),
//                               "%s is a file, cannot create directory");
      // TODO TH: what if repo does not exists?
      if (localMavenRepo.isDirectory()) {
         getLogger().debug("Using local maven repository {}.", localMavenRepo);
         session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(
               session,
               new LocalRepository(localMavenRepo)));
      }

      // TODO TH: what if no remote repository is configured.
      remoteRepositories.add(
            new RemoteRepository.Builder("remote", "default", repositoryConfiguration.getRemoteRepositoryUrl())
//                  .setAuthentication(new AuthenticationBuilder()
//                                           .addUsername(repositoryConfiguration.getRemoteRepositoryUsername())
//                                           .addPassword(repositoryConfiguration.getRemoteRepositoryPassword())
//                                           .build())
                  .build()
      );

      return session;
   }

   private void resolveDependency(Dependency dependency,
                                  RepositorySystem repositorySystem,
                                  RepositorySystemSession session) {
      if (dependency instanceof ModuleDependency) {
         doResolveDependency((ModuleDependency) dependency);
      } else {
         doResolveDependency(dependency);
      }
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

   public IRepositoryConfiguration getRepositoryConfiguration() {
      return repositoryConfiguration;
   }

   public PopulateMaven2Repository setRepositoryConfiguration(
         IRepositoryConfiguration repositoryConfiguration) {
      this.repositoryConfiguration = repositoryConfiguration;
      return this;
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

   private void doResolveDependency(Dependency dependency) {
      getLogger().lifecycle("Attempting to resolve artifacts for '{}:{}:{}'.",
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
         getLogger().lifecycle("Attempting to resolve artifacts for '{}:{}:{}'.",
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
      String logMsg = String.format("%s:%s:%s%s@%s",
                                    groupId,
                                    artifactId,
                                    version,
                                    classifier == null ? "" : ":" + classifier,
                                    extension);
      getLogger().lifecycle("Retrieving '{}' and its dependencies (download may be necessary) ...", logMsg);

      CollectRequest request = new CollectRequest();
      Artifact baseArtifact = classifier == null
                              ? new DefaultArtifact(groupId, artifactId, extension, version)
                              : new DefaultArtifact(groupId, artifactId, classifier, extension, version);
      request.setRoot(new org.eclipse.aether.graph.Dependency(baseArtifact, null));
      request.setRepositories(remoteRepositories);

      DependencyRequest dependencyRequest = new DependencyRequest(request, null);
      try {
         DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);
         for(ArtifactResult localArtifact : result.getArtifactResults()) {
            getLogger().lifecycle("Located {}.", localArtifact.getArtifact().getFile());
         }
      } catch (DependencyResolutionException e) {
         if(e.getCause() instanceof ArtifactResolutionException) {
            getLogger().lifecycle("Did not find artifact '" + logMsg + "'.");
            //getLogger().debug("Exception was:", e);
         } else {
            getLogger().error("Error while resolving '" + logMsg + "'.", e);
         }
      }

      return null;
   }

}
