package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.tasks.DefaultTaskAction;
import com.ngc.seaside.gradle.util.GradleUtil;

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
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves dependencies using the Maven Aether API.
 */
public class ResolveDependenciesAction extends DefaultTaskAction<PopulateMaven2Repository> {

   /**
    * The default classifiers to use when attempting to resolve a dependency.  This includes {@code null}, "sources",
    * and "tests".  Note {@code null} indicates that the "real" artifact of the dependency (ie, the actual JAR file)
    * should attempt to be resolved.
    */
   private final static List<String> DEFAULT_CLASSIFIERS = Collections.unmodifiableList(
         Arrays.asList(null, "sources", "tests", "javadoc"));

   /**
    * The default extension to use when using the default classifiers.  We need this in case the build uses the defaults
    * and does not specific the classifiers or extensions directly.
    */
   private final static String DEFAULT_EXTENSION = "jar";

   /**
    * The dependencies that have been resolved.
    */
   private final Collection<DependencyResult> dependencyResults = new ArrayList<>();

   /**
    * The set of artifacts (including transitive dependencies) that have been resolved.  These artifacts were resolved
    * with only the default classifier (null) so it may be necessary to resolve additional classifiers for these
    * artifacts.
    */
   private final Set<ArtifactKey> transitiveDependenciesWithMissingClassifiers = new HashSet<>();

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
    * The total dependencies resolved thus far (not including transitive dependencies).
    */
   private long totalDependenciesRetrieved = 0;

   /**
    * If true, extra classifiers for transitive dependencies are being resolved.  This means we don't want to
    * add the artifact to the list of transitive dependencies again.
    */
   private boolean transitiveClassifierResolutionInProgress = false;

   @Override
   public void validate(PopulateMaven2Repository task) throws InvalidUserDataException {
      GradleUtil.checkUserData(task.getLocalRepository() != null,
                               "local repository not set!");
      GradleUtil.checkUserData(
            task.getRemoteRepository() != null || Files.isDirectory(Paths.get(task.getLocalRepository().getUrl())),
            "since local repository %s is not a directory a remote repository must be configured!",
            task.getLocalRepository().getUrl());
   }

   public Collection<DependencyResult> getDependencyResults() {
      return dependencyResults;
   }

   @Override
   protected void doExecute() {
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
      logger.lifecycle("{} dependencies must be resolved.", totalDependenciesRequired);

      // Resolve each dependency.
      for (Configuration config : configs) {
         logger.lifecycle("Resolving dependencies for configuration {}.", config.getName());
         for (Dependency dependency : config.getDependencies()) {
            resolveDependency(dependency);
         }
      }

      // Try to resolve any additional classifiers for transitive dependencies.
      resolveExtraClassifiersForTransitiveDependencies();
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

      File localMavenRepo = Paths.get(task.getLocalRepository().getUrl()).toFile();
      session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(
            session,
            new LocalRepository(localMavenRepo)));

      // Most of the time, we want this value to be true.  This prevents Maven/Gradle from reaching out
      // to other repositories.
      session.setIgnoreArtifactDescriptorRepositories(task.isIgnoreArtifactDescriptorRepositories());
      // Most of the time, we want to set this value to UPDATE_POLICY_NEVER.  Otherwise, Maven may change for updates
      // for artifacts that use version ranges.  This can *really* slow down the build.  This can happen if transitive
      // dependencies use a version range.  A new version of the artifact will be checked on every build unless the
      // policy is set to never.
      session.setUpdatePolicy(task.getRepositoryUpdatePolicy());

      return session;
   }


   /**
    * Creates the remote repositories to resolve artifacts from.
    */
   protected List<RemoteRepository> createRemoteRepositories() {
      List<RemoteRepository> repos = new ArrayList<>();
      if (task.getRemoteRepository() != null) {
         RemoteRepository.Builder repoBuilder = new RemoteRepository.Builder(
               task.getRemoteRepository().getName(),
               "default",
               task.getRemoteRepository().getUrl().toString());

         if (task.getRemoteRepository().getCredentials() != null
             && task.getRemoteRepository().getCredentials().getUsername() != null
             && task.getRemoteRepository().getCredentials().getPassword() != null) {
            repoBuilder.setAuthentication(new AuthenticationBuilder()
                                                .addUsername(task.getRemoteRepository().getCredentials().getUsername())
                                                .addPassword(task.getRemoteRepository().getCredentials().getPassword())
                                                .build());
         }

         repos.add(repoBuilder.build());
      } else {
         logger.lifecycle("No remote repository configured, downloads will not be attempted.");
      }

      return repos;
   }

   /**
    * Template method invoked to handle an exception that was encountered while resolving dependencies.  Simply logs the
    * exception if the exception if on concern.  The default implementation simply logs and consumes the exception.
    */
   protected void handleResolutionException(DependencyResolutionException e,
                                            String groupId,
                                            String artifactId,
                                            String version,
                                            String classifier,
                                            String extension,
                                            String prettyGave) {
      if (e.getCause() instanceof ArtifactResolutionException) {
         // If the artifact was not resolved and it is one of the default classifiers (ie, sources or tests), it's
         // no big deal if that artifact wasn't found.  Just let the user know not to worry.
         if (classifier != null && DEFAULT_CLASSIFIERS.contains(classifier)) {
            logger.lifecycle("Did not resolve '{}' but that is okay since that artifact is only {}.",
                             prettyGave,
                             classifier);
         } else {
            // Otherwise, we didn't find something that may actually be important.
            logger.warn("Failed to resolve '{}' (this artifact may be required).", prettyGave, e);
         }
      } else {
         // This means something else failed.
         logger.error("Encountered unexpected error while resolving '{}'.", prettyGave, e);
      }
   }

   /**
    * Template method invoked to handle the result of resolving artifacts.  Default implementation adds the result to
    * the {@link #getDependencyResults() collection}.
    */
   protected void handleDependencyResult(DependencyResult result) {
      for (ArtifactResult localArtifact : result.getArtifactResults()) {
         logger.lifecycle("Located {}.", localArtifact.getArtifact().getFile());
         if (!transitiveClassifierResolutionInProgress) {
            transitiveDependenciesWithMissingClassifiers.add(new ArtifactKey(
                  localArtifact.getArtifact().getGroupId(),
                  localArtifact.getArtifact().getArtifactId(),
                  localArtifact.getArtifact().getVersion()));
         }
      }
      dependencyResults.add(result);
   }

   /**
    * Gets the configurations whose dependencies should be resolved.
    */
   private Collection<Configuration> getConfigurations() {
      Collection<Configuration> configs;
      if (task.getConfiguration() != null) {
         configs = Collections.singleton(task.getConfiguration());
      } else {
         Project project = task.getProject();
         configs = new ArrayList<>(project.getConfigurations());
         // Include the configurations from the build script.
         configs.addAll(project.getBuildscript().getConfigurations());
         // If there is a real root project, be sure to add the configurations of that build script to the configuration
         // to resolve since those configurations will be used to build this project as well.
         if (!project.equals(project.getRootProject())) {
            configs.addAll(project.getRootProject().getBuildscript().getConfigurations());
         }
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
         logger.lifecycle("[{}/{}] Dependency '{}:{}:{}' is self resolving (it's probably a dependency directly"
                          + " on a project), ignoring it.",
                          totalDependenciesRetrieved + 1,
                          totalDependenciesRequired,
                          dependency.getGroup(),
                          dependency.getName(),
                          dependency.getVersion());
      } else if (dependency.getGroup() == null || dependency.getGroup().trim().isEmpty()) {
         // Sometimes, the group can be empty.  This usually means the dependency is declared as a file located in
         // some directory.  In that case, we ignore it since there is POM file with it.
         logger.lifecycle("[{}/{}] Dependency '{}:{}:{}' has no group ID (it's probably a dependency directly"
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
    * Resolves the given {@code Dependency}.
    */
   private void doResolveDependency(Dependency dependency) {
      logger.lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
                       totalDependenciesRetrieved + 1,
                       totalDependenciesRequired,
                       dependency.getGroup(),
                       dependency.getName(),
                       dependency.getVersion());

      // Just resolve the default classifier for now.  We will get the additional classifiers (IE, javadoc, sources,
      // etc) when we resolve the classifiers for all transitive dependencies.
      getDependencyResult(dependency.getGroup(),
                          dependency.getName(),
                          dependency.getVersion(),
                          DEFAULT_CLASSIFIERS.get(0),
                          DEFAULT_EXTENSION)
            .ifPresent(this::handleDependencyResult);
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
         logger.lifecycle("[{}/{}] Attempting to resolve artifacts for '{}:{}:{}'.",
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

   private void resolveExtraClassifiersForTransitiveDependencies() {
      // The number of artifacts we have resolved extra classifiers for (so far).
      int resolved = 0;
      logger.lifecycle("Extra classifiers for {} transitive dependencies must be resolved.",
                       transitiveDependenciesWithMissingClassifiers.size());
      // Set this to true so we don't keep adding the same things to the list of transitive dependencies again.
      // We don't reset this in a try/finally because any exception will abort the entire build anyway.
      transitiveClassifierResolutionInProgress = true;

      for (ArtifactKey key : transitiveDependenciesWithMissingClassifiers) {
         logger.lifecycle("[{}/{}] Attempting to resolve additional classifiers for transitive dependency '{}:{}:{}'.",
                          resolved + 1,
                          transitiveDependenciesWithMissingClassifiers.size(),
                          key.getGroupId(),
                          key.getArtifactId(),
                          key.getVersion());

         // Try to resolve the dependency along with any extra classifiers.
         // Skip the first default classifier because we already have that file.  We just want the other
         // classifiers.
         for (String classifier : DEFAULT_CLASSIFIERS.subList(1, DEFAULT_CLASSIFIERS.size())) {
            getDependencyResult(key.getGroupId(),
                                key.getArtifactId(),
                                key.getVersion(),
                                classifier,
                                DEFAULT_EXTENSION)
                  .ifPresent(this::handleDependencyResult);
         }

         resolved++;
      }

      // Reset state.
      transitiveClassifierResolutionInProgress = false;
      transitiveDependenciesWithMissingClassifiers.clear();
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
      String remoteLogMsg = task.getRemoteRepository() == null ? "no remote repository configured, no download possible"
                                                               : "download may be required";
      logger.lifecycle("Retrieving '{}' and its dependencies ({}) ...", prettyGave, remoteLogMsg);

      // Make API stuff.
      CollectRequest request = new CollectRequest();
      Artifact baseArtifact = classifier == null
                              ? new DefaultArtifact(groupId, artifactId, extension, version)
                              : new DefaultArtifact(groupId, artifactId, classifier, extension, version);
      request.setRoot(new org.eclipse.aether.graph.Dependency(baseArtifact, null));
      request.setRepositories(remoteRepositories);

      DependencyRequest dependencyRequest = new DependencyRequest(request, null);
      try {
         // Resolve the dependency, including transitive dependencies.  This will not return until they are resoled or
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
}
