package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import groovy.lang.Closure;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.aether.repository.RepositoryPolicy;
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
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 * This task will create a directory in a maven2 layout for the dependencies of a project.  By default, it resolves all
 * dependencies of all configurations declared in the project.  This can be overridden by specifying the configuration
 * to use directly via {@link #setConfiguration(Configuration)}.  A typical usage of this task looks like this:
 * <pre>
 * {@code
 * task m2repo(type: PopulateMaven2Repository) {
 *   // This is the directory to save the repo to.
 *   outputDirectory = project.file("${project.buildDir}/m2")
 *   // Use the remote Nexus repo we have already configured as part of the parent plugin.
 *   remoteRepository = project.repositories.getByName('NexusConsolidated')
 *   // Use the default maven local repository.  This will respect the $M2_HOME/conf/settings.xml configuration.
 *   localRepository = mavenLocal()
 * }
 * }
 * </pre>
 */
public class PopulateMaven2Repository extends DefaultTask {

   /**
    * The default classifiers to use when attempting to resolve a dependency.  This includes {@code null}, "sources",
    * and "tests".  Note {@code null} indicates that the "real" artifact of the dependency (ie, the actual JAR file)
    * should attempt to be resolved.
    */
   private final static Collection<String> DEFAULT_CLASSIFIERS = Collections.unmodifiableCollection(
         Arrays.asList(null, "sources", "tests"));

   /**
    * The default extension to use when using the default classifiers.  We need this in case the build uses the defaults
    * and does not specific the classifiers or extensions directly.
    */
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
   private List<RemoteRepository> remoteRepositories = new ArrayList<>();

   /**
    * The total number of dependencies that are required.  This does not include transitive dependencies.
    */
   private long totalDependenciesRequired = 0;

   /**
    * The total dependencies resolved thus far (not including transitive dependencies.
    */
   private long totalDependenciesRetrieved = 0;

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

   /**
    * If true, the dependency will be resolved (and downloaded to the local repository) but they won't actually be
    * copied to the output directory.  The default is false.
    */
   private boolean populateLocalRepoOnly = false;

   /**
    * If true, repositories that are listed in the POM of dependencies will be ignored when attempting to resolve
    * missing items.
    */
   private boolean ignoreArtifactDescriptorRepositories = true;

   /**
    * The update policy to use when resolving dependencies.
    */
   private String repositoryUpdatePolicy = RepositoryPolicy.UPDATE_POLICY_NEVER;

   /**
    * Used to create instances of {@code MavenArtifactRepository} for ease of user configuration.  Provided by Gradle at
    * runtime.
    */
   private final BaseRepositoryFactory baseRepositoryFactory;

   @Inject
   public PopulateMaven2Repository(BaseRepositoryFactory baseRepositoryFactory) {
      this.baseRepositoryFactory = baseRepositoryFactory;
   }

   @TaskAction
   public void populateRepository() {
      Preconditions.checkState(populateLocalRepoOnly || outputDirectory != null,
                               "outputDirectory must be set if populateLocalRepoOnly is false!");
      Preconditions.checkState(localRepository != null,
                               "local repository not set!");
      Preconditions.checkState(remoteRepository != null || Files.isDirectory(Paths.get(localRepository.getUrl())),
                               "since local repository %s is not a directory a remote repository must be configured!",
                               localRepository.getUrl());

      // Initialize the Maven API.
      repositorySystem = newRepositorySystem();
      session = newSession(repositorySystem);
      remoteRepositories = createRemoteRepositories();

      // Get the configurations for which we must retrieve dependencies for.
      Collection<Configuration> configs = getConfigurations();

      // Add helpful logging about progress.
      totalDependenciesRequired = configs.stream()
            .mapToLong(c -> c.getDependencies().size())
            .sum();
      getLogger().lifecycle("{} dependencies must be resolved.", totalDependenciesRequired);

      // Resolve each dependency.
      for (Configuration config : configs) {
         getLogger().lifecycle("Resolving dependencies for configuration {}.", config.getName());
         for (Dependency dependency : config.getDependencies()) {
            resolveDependency(dependency);
         }
      }
   }

   /**
    * Creates a new Maven repository and allows an action to configure it.  This enables DSL syntax in the task
    * configuration to configure a custom remote or local repository.
    *
    * @param action the action to apply to the repository
    * @return the repository
    */
   public MavenArtifactRepository maven(Action<? super MavenArtifactRepository> action) {
      MavenArtifactRepository repo = baseRepositoryFactory.createMavenRepository();
      action.execute(repo);
      return repo;
   }

   /**
    * Creates a new Maven repository and allows an closure to configure it.  This enables DSL syntax in the task
    * configuration to configure a custom remote or local repository.
    *
    * @param closure the closure to apply to the repository
    * @return the repository
    */
   public MavenArtifactRepository maven(Closure closure) {
      return maven(ConfigureUtil.configureUsing(closure));
   }

   /**
    * Creates a new Maven local repository.  This enables DSL syntax in the task configuration to configure a local
    * repository.  The repository location is resolved using the standard rules.  IE, read $M2_HOME/conf/settings.xml,
    * try $USER/.m2, etc.
    */
   public MavenArtifactRepository mavenLocal() {
      return baseRepositoryFactory.createMavenLocalRepository();
   }

   /**
    * Gets the output directory configured for use with the task.  Dependencies will be copied to this directory in an
    * M2 layout.
    */
   @OutputDirectory
   public File getOutputDirectory() {
      return outputDirectory;
   }

   /**
    * Sets the output directory configured for use with the task.  Dependencies will be copied to this directory in an
    * M2 layout.
    */
   public void setOutputDirectory(File outputDirectory) {
      this.outputDirectory = Preconditions.checkNotNull(outputDirectory, "outputDirectory may not be null!");
   }

   /**
    * Sets the output directory configured for use with the task.  Dependencies will be copied to this directory in an
    * M2 layout.  This method allows a user to specify the output directory as a command line option.
    */
   @Option(option = "outputDirectory",
         description = "The directory to place the dependencies of the project in maven2 layout.")
   public void setOutputDirectory(String outputDirectory) {
      Preconditions.checkNotNull(outputDirectory, "outputDirectory may not be null!");
      Preconditions.checkArgument(!outputDirectory.trim().isEmpty(), "outputDirectory may not be null!");
      setOutputDirectory(new File(outputDirectory));
   }

   /**
    * Gets the configuration to retrieve dependencies for.  If this value is {@code null}, dependencies will be
    * retrieved for all configurations used by the project.
    */
   public Configuration getConfiguration() {
      return configuration;
   }

   /**
    * Sets the configuration to retrieve dependencies for.  If this value is {@code null}, dependencies will be
    * retrieved for all configurations used by the project.
    */
   public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
   }

   /**
    * Gets the remove Maven repository to use to resolve dependencies.
    */
   public MavenArtifactRepository getRemoteRepository() {
      return remoteRepository;
   }

   /**
    * Sets the remove Maven repository to use to resolve dependencies.
    */
   public void setRemoteRepository(MavenArtifactRepository remoteRepository) {
      this.remoteRepository = remoteRepository;
   }

   /**
    * Gets the local Maven repository to use to cache downloaded files.
    */
   public MavenArtifactRepository getLocalRepository() {
      return localRepository;
   }

   /**
    * Sets the local Maven repository to use to cache downloaded files.
    */
   public void setLocalRepository(MavenArtifactRepository localRepository) {
      this.localRepository = localRepository;
   }

   /**
    * If true, only the local Maven repository will be populated with downloaded files and no artifacts will be copied
    * to {@code outputDirectory}.
    */
   public boolean isPopulateLocalRepoOnly() {
      return populateLocalRepoOnly;
   }

   /**
    * If true, only the local Maven repository will be populated with downloaded files and no artifacts will be copied
    * to {@code outputDirectory}.
    */
   public void setPopulateLocalRepoOnly(boolean populateLocalRepoOnly) {
      this.populateLocalRepoOnly = populateLocalRepoOnly;
   }

   /**
    * If true, repositories that are listed in the POM of dependencies will be ignored when attempting to resolve
    * missing items.
    */
   public boolean isIgnoreArtifactDescriptorRepositories() {
      return ignoreArtifactDescriptorRepositories;
   }

   /**
    * If true, repositories that are listed in the POM of dependencies will be ignored when attempting to resolve
    * missing items.
    */
   public PopulateMaven2Repository setIgnoreArtifactDescriptorRepositories(
         boolean ignoreArtifactDescriptorRepositories) {
      this.ignoreArtifactDescriptorRepositories = ignoreArtifactDescriptorRepositories;
      return this;
   }

   /**
    * The update policy to use when resolving dependencies.
    */
   public String getRepositoryUpdatePolicy() {
      return repositoryUpdatePolicy;
   }

   /**
    * The update policy to use when resolving dependencies.
    */
   public PopulateMaven2Repository setRepositoryUpdatePolicy(String repositoryUpdatePolicy) {
      this.repositoryUpdatePolicy = repositoryUpdatePolicy;
      return this;
   }

   /**
    * Creates a new {@code RepositorySystem} that can be used to make requests.
    */
   protected RepositorySystem newRepositorySystem() {
      DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
      locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
      locator.addService(TransporterFactory.class, FileTransporterFactory.class);
      locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
      return locator.getService(RepositorySystem.class);
   }

   /**
    * Creates a new {@code RepositorySystemSession} which can be used to resolve artifacts.  {@code localRepository}
    * must be set before invoking this method.
    */
   protected RepositorySystemSession newSession(RepositorySystem repositorySystem) {
      DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

      File localMavenRepo = Paths.get(localRepository.getUrl()).toFile();
      session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(
            session,
            new LocalRepository(localMavenRepo)));

      // Most of the time, we want this value to be true.  This prevents Maven/Gradle from reaching out
      // to other repositories.
      session.setIgnoreArtifactDescriptorRepositories(ignoreArtifactDescriptorRepositories);
      // Most of the time, we want to set this value to UPDATE_POLICY_NEVER.  Otherwise, Maven may change for updates
      // for artifacts that use version ranges.  This can *really* slow down the build.  This can happen if transitive
      // dependencies use a version range.  A new version of the artifact will be checked on every build unless the
      // policy is set to never.
      session.setUpdatePolicy(repositoryUpdatePolicy);

      return session;
   }

   /**
    * Creates the remote repositories to resolve artifacts from.
    */
   protected List<RemoteRepository> createRemoteRepositories() {
      List<RemoteRepository> repos = new ArrayList<>();
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

         repos.add(repoBuilder.build());
      } else {
         getLogger().lifecycle("No remote repository configured, downloads will not be attempted.");
      }

      return repos;
   }

   /**
    * Gets the configurations whose dependencies should be resolved.
    */
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
      // A self resolving dependency is a dependency that can be resolved without a repository.  Dependencies on
      // projects (ie, compile project(":name")) take this form.  Also, dependencies on flat directories on the
      // file system take this form.  If this is the case, we don't want to put these dependencies with a maven2
      // layout because we may not have a POM to go with it.
      if (dependency instanceof SelfResolvingDependency) {
         // Basically ignore this dependency.
         getLogger().lifecycle("[{}/{}] Dependency '{}:{}:{}' is self resolving (it's probably a dependency directly"
                               + " on a project), ignoring it.",
                               totalDependenciesRetrieved + 1,
                               totalDependenciesRequired,
                               dependency.getGroup(),
                               dependency.getName(),
                               dependency.getVersion());
      } else if (dependency.getGroup() == null || dependency.getGroup().trim().isEmpty()) {
         // Sometimes, the group can be empty.  This usually means the dependency is declared as a file located in
         // some directory.  In that case, we ignore it since there is POM file with it.
         getLogger().lifecycle("[{}/{}] Dependency '{}:{}:{}' has no group ID (it's probably a dependency directly"
                               + " on a file), ignoring it.",
                               totalDependenciesRetrieved + 1,
                               totalDependenciesRequired,
                               dependency.getGroup(),
                               dependency.getName(),
                               dependency.getVersion());
      } else if (dependency instanceof ModuleDependency) {
         doResolveDependency((ModuleDependency) dependency);
      } else {
         doResolveDependency(dependency);
      }
      totalDependenciesRetrieved++;
   }

   /**
    * Resolves the given {@code Dependency}.  The artifacts for the {@link #DEFAULT_CLASSIFIERS} will also be resolved
    * if they exists.
    */
   private void doResolveDependency(Dependency dependency) {
      getLogger().lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
                            totalDependenciesRetrieved + 1,
                            totalDependenciesRequired,
                            dependency.getGroup(),
                            dependency.getName(),
                            dependency.getVersion());

      for (String classifier : DEFAULT_CLASSIFIERS) {
         getDependencyResult(dependency.getGroup(),
                             dependency.getName(),
                             dependency.getVersion(),
                             classifier,
                             DEFAULT_EXTENSION)
               .ifPresent(this::handleDependencyResult);
      }
   }

   /**
    * Resolves the given {@code ModuleDependency}.
    */
   private void doResolveDependency(ModuleDependency dependency) {
      // If the dependency has no artifacts, we need to resolve the dependency directly and try to find the default
      // artifacts/classifiers.
      if (dependency.getArtifacts().isEmpty()) {
         doResolveDependency((Dependency) dependency);
      } else {
         // Otherwise, the build has specified the particular artifacts of the dependency that are required.
         // Print progress.
         getLogger().lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
                               totalDependenciesRetrieved + 1,
                               totalDependenciesRequired,
                               dependency.getGroup(),
                               dependency.getName(),
                               dependency.getVersion());
         for (DependencyArtifact artifact : dependency.getArtifacts()) {
            // Get the dependency and handle the result if we were able to resolve it successfully.
            getDependencyResult(dependency.getGroup(),
                                dependency.getName(),
                                dependency.getVersion(),
                                artifact.getClassifier(),
                                artifact.getExtension())
                  .ifPresent(this::handleDependencyResult);
         }
      }
   }

   /**
    * Attempt to resolve the given dependency.
    *
    * @return an optional containing the result; if the dependency could not be resolved the optional is empty
    */
   private Optional<DependencyResult> getDependencyResult(String groupId,
                                                          String artifactId,
                                                          String version,
                                                          String classifier,
                                                          String extension) {
      DependencyResult result = null;

      // The pretty form of the dependency (used for logging).
      String prettyGave = String.format("%s:%s:%s%s@%s",
                                        groupId,
                                        artifactId,
                                        version,
                                        classifier == null ? "" : ":" + classifier,
                                        extension);
      String remoteLogMsg = remoteRepository == null ? "no remote repository configured, no download possible"
                                                     : "download may be required";
      getLogger().lifecycle("Retrieving '{}' and its dependencies ({}) ...", prettyGave, remoteLogMsg);

      // Make API stuff.
      CollectRequest request = new CollectRequest();
      Artifact baseArtifact = classifier == null
                              ? new DefaultArtifact(groupId, artifactId, extension, version)
                              : new DefaultArtifact(groupId, artifactId, classifier, extension, version);
      request.setRoot(new org.eclipse.aether.graph.Dependency(baseArtifact, null));
      request.setRepositories(remoteRepositories);

      DependencyRequest dependencyRequest = new DependencyRequest(request, null);
      try {
         // Resolve the dependency, including transitive dependencies.  This will not return until they are resovled or
         // an error happens.
         result = repositorySystem.resolveDependencies(session, dependencyRequest);
      } catch (DependencyResolutionException e) {
         handleResolutionException(e,
                                   groupId,
                                   artifactId,
                                   version,
                                   classifier,
                                   extension,
                                   prettyGave);
      }

      return Optional.ofNullable(result);
   }

   /**
    * Handles the result of resolving artifacts.  If {@code populateLocalRepoOnly} is false, the artifacts are copied to
    * {@code outputDirectory}.
    */
   private void handleDependencyResult(DependencyResult result) {
      for (ArtifactResult localArtifact : result.getArtifactResults()) {
         getLogger().lifecycle("Located {}.", localArtifact.getArtifact().getFile());

         // If the dependency has a scope of "system", the file path may be relative.
         // If that is the case, we want to ignore the file.
         if (!populateLocalRepoOnly && !localArtifact.getArtifact().getFile().toString().contains("..")) {
            File artifact = localArtifact.getArtifact().getFile();
            copyFileToOutputDirectory(artifact.toPath());

            // Find and copy any POM files directly since the API does not expose POMs.
            if (artifact.getParentFile() != null) {
               FileUtils.listFiles(artifact.getParentFile(), new String[]{"pom"}, false)
                     .forEach(f -> copyFileToOutputDirectory(f.toPath()));
            }
         }
      }
   }

   /**
    * Copies a file that resides in the local repository to the output directory, maintaining the directory structure of
    * the file relative to the local repository location.  IE, this keeps the groupId/artifactId/version/ directory
    * structure for Maven M2 layouts.
    */
   private void copyFileToOutputDirectory(Path path) {
      // The path to the artifact inside the local repository.
      path = path.toAbsolutePath();
      // The path to the local repository.
      Path localRepo = Paths.get(localRepository.getUrl()).toAbsolutePath();
      // The path to the artifact inside the local repository that is relative to the local repository.  This
      // gives us the path that starts the group ID, then the artifact ID, then the version, etc.
      Path relativeArtifactPath = localRepo.relativize(path);
      // The destination file.  This is the relative path resolved against the output directory.  Only copy the
      // file if needed.
      Path dest = outputDirectory.toPath().resolve(relativeArtifactPath);

      if (!Files.exists(dest)) {
         // Create the directory structure if needed.
         Path parent = dest.getParent();
         try {
            if (parent != null) {
               // Note this is safe even if the directory already exists.
               Files.createDirectories(parent);
            }
            Files.copy(path, dest);
         } catch (IOException e) {
            getLogger().error("Unexpected error while copying {} to {}.", path, dest, e);
         }
      }
   }

   /**
    * Handles an exception that was encountered while resolving dependencies.  Simply logs the exception if the
    * exception if on concern.
    */
   private void handleResolutionException(DependencyResolutionException e,
                                          String groupId,
                                          String artifactId,
                                          String version,
                                          String classifier,
                                          String extension,
                                          String prettyGave) {
      if (e.getCause() instanceof ArtifactResolutionException) {
         // If the artifact was not resolved and it is one of the default classifiers (ie, sources or tests), it's
         // no big deal if that artifact wasn't found.  Just let the user know not to worry.
         if (DEFAULT_CLASSIFIERS.contains(classifier)) {
            getLogger().lifecycle("Did not resolve '{}' but that is okay since that artifact is only {}.",
                                  prettyGave,
                                  classifier);
         } else {
            // Otherwise, we didn't find something that may actually be important.
            getLogger().warn("Failed to resolve '{}' (this artifact may be required).", prettyGave);
         }
      } else {
         // This means something else failed.
         getLogger().error("Encountered unexpected error while resolving '{}'.", prettyGave, e);
      }
   }
}
