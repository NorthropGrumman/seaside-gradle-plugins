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

import com.google.common.base.Preconditions;

import org.apache.commons.io.FileUtils;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import groovy.lang.Closure;

/**
 * This task will create a directory in a maven2 layout for the dependencies of a project.  By default, it resolves all
 * dependencies of all configurations declared in the project.  This can be overridden by specifying the configuration
 * to use directly via {@link #setConfigurations(List)}.  A typical usage of this task looks like this:
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
    * The user configured output directory to populate.
    */
   private File outputDirectory;

   /**
    * The file that will be written that contains the dependency info.
    */
   private File dependencyInfoReportFile;

   /**
    * The file to create for the deployment script.
    */
   private File deploymentScriptFile;

   /**
    * The configurations in which to resolve the dependencies.
    * If not set, dependencies for all configurations will be resolved
    */
   private List<Configuration> configs;

   /**
    * The names of the configurations which must be resolved prior to determining their dependencies.  This allows
    * configurations with default dependencies to set their dependencies.  See {@link
    * Configuration#defaultDependencies(Action)}.
    */
   private Collection<String> configurationsToResolve = new HashSet<>();

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
    * If true, a dependency report will be created.
    */
   private boolean createDependencyReportFile = true;

   /**
    * If true, snapshot dependencies that were added to the local maven repository as a result of this build will be
    * removed.  This is usually done only on CI servers.
    */
   private boolean removeSnapshots = false;

   /**
    * If true, a deployment script file will be created.
    */
   private boolean createDeploymentScriptFile = true;

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
      ResolveDependenciesAction resolveDependencies = newResolveDependenciesAction();
      CopyDependencyFilesAction copyDependencyFiles = newCopyDependencyFilesAction();
      CreateDependencyReportAction createCsvDependencyReport = newCreateDependencyReportAction();
      CreateDeploymentScriptAction createDeploymentScript = newCreateDeploymentScriptAction();
      RemoveSnapshotsAction removeSnapshots = newRemoveSnapshotsAction();

      resolveDependencies.validate(this);
      copyDependencyFiles.validate(this);
      createCsvDependencyReport.validate(this);
      createDeploymentScript.validate(this);
      removeSnapshots.validate(this);

      resolveDependencies.execute(this);

      copyDependencyFiles.setDependencyResults(resolveDependencies.getDependencyResults());
      copyDependencyFiles.execute(this);

      // We can't generate scripts if populateLocalRepoOnly is true.
      if (!populateLocalRepoOnly) {
         ArtifactResultStore store = createStore(resolveDependencies.getDependencyResults());
         createCsvDependencyReport.setStore(store);
         createCsvDependencyReport.execute(this);
      }

      createDeploymentScript.execute(this);

      removeSnapshots.setDependencyResults(resolveDependencies.getDependencyResults());
      removeSnapshots.execute(this);
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
   public MavenArtifactRepository maven(Closure<?> closure) {
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
    * Gets the file that will be written that contains the dependency info.
    */
   @OutputFile
   @org.gradle.api.tasks.Optional
   public File getDependencyInfoReportFile() {
      return dependencyInfoReportFile;
   }

   /**
    * Sets the file that will be written that contains the dependency info.
    */
   public void setDependencyInfoReportFile(File dependencyInfoReportFile) {
      this.dependencyInfoReportFile = Preconditions.checkNotNull(dependencyInfoReportFile,
                                                                 "dependencyInfoReportFile may not be null!");
   }

   /**
    * Sets the file that will be written that contains the dependency info.  This method allows a user to specify the
    * output directory as a command line option.
    */
   @Option(option = "dependencyInfoReportFile",
         description = "The report file to output which contains the dependency information.")
   public void setDependencyInfoReportFile(String dependencyInfoReportFile) {
      Preconditions.checkNotNull(dependencyInfoReportFile, "dependencyInfoReportFile may not be null!");
      Preconditions.checkArgument(!dependencyInfoReportFile.trim().isEmpty(),
                                  "dependencyInfoReportFile may not be null!");
      setDependencyInfoReportFile(new File(dependencyInfoReportFile));
   }

   /**
    * Gets the file to use when creating the deployment script.
    */
   @OutputFile
   @org.gradle.api.tasks.Optional
   public File getDeploymentScriptFile() {
      return deploymentScriptFile;
   }

   /**
    * Sets the file to use when creating the deployment script.
    */
   public void setDeploymentScriptFile(File deploymentScriptFile) {

      this.deploymentScriptFile = Preconditions.checkNotNull(deploymentScriptFile,
                                                             "deploymentScriptFile may not be null!");
   }

   /**
    * Sets the file to use when creating the deployment script.  This method allows a user to specify the output
    * directory as a command line option.
    */
   @Option(option = "deploymentScriptFile",
         description = "The report file to output which contains the dependency information.")
   public void setDeploymentScriptFile(String deploymentScriptFile) {
      Preconditions.checkNotNull(deploymentScriptFile, "deploymentScriptFile may not be null!");
      Preconditions.checkArgument(!deploymentScriptFile.trim().isEmpty(),
                                  "deploymentScriptFile may not be null!");
      setDeploymentScriptFile(new File(deploymentScriptFile));
   }

   /**
    * If true, snapshot dependencies that were added to the local maven repository as a result of this build will be
    * removed.  This is usually done only on CI servers.
    */
   public boolean isRemoveSnapshots() {
      return removeSnapshots;
   }

   /**
    * Sets the value that indicates if snapshot dependencies that were added to the local maven repository as a result
    * of this build will be removed.  This is usually done only on CI servers.
    */
   public void setRemoveSnapshots(boolean removeSnapshots) {
      this.removeSnapshots = removeSnapshots;
   }

   /**
    * Sets the value that indicates if snapshot dependencies that were added to the local maven repository as a result
    * of this build will be removed.  This is usually done only on CI servers.  This method allows a user to specify the
    * output CSV file as a command line option.
    */
   @Option(option = "removeSnapshots",
         description = "If true, snapshots installed to the local maven repository will be removed.")
   public void setRemoveSnapshots(String removeSnapshots) {
      Preconditions.checkNotNull(removeSnapshots, "removeSnapshots may not be null!");
      Preconditions.checkArgument(!removeSnapshots.trim().isEmpty(), "removeSnapshots may not be null!");
      setRemoveSnapshots(Boolean.valueOf(removeSnapshots));
   }


   /**
    * Gets the configuration to retrieve dependencies for.  If this value is {@code null}, dependencies will be
    * retrieved for all configurations used by the project.
    */
   public List<Configuration> getConfigurations() {
      return configs;
   }

   /**
    * Sets the configurations in which to retrieve dependencies.  If this value is {@code null}, dependencies will be
    * retrieved for all configurations used by the project.
    */
   public void setConfigurations(List<Configuration> configs) {
      this.configs = configs;
   }

   /**
    * Gets the names of the configurations which must be resolved prior to determining their dependencies.  This allows
    * configurations with default dependencies to set their dependencies.  See {@link
    * Configuration#defaultDependencies(Action)}.
    */
   public Collection<String> getConfigurationsToResolve() {
      return configurationsToResolve;
   }

   /**
    * Sets the names of the configurations which must be resolved prior to determining their dependencies. This allows
    * configurations with default dependencies to set their dependencies.  See {@link
    * Configuration#defaultDependencies(Action)}.
    */
   public void setConfigurationsToResolve(Collection<String> configurationsToResolve) {
      this.configurationsToResolve = Preconditions.checkNotNull(configurationsToResolve,
                                                                "configurationsToResolve may not be null!");
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
   public void setIgnoreArtifactDescriptorRepositories(
         boolean ignoreArtifactDescriptorRepositories) {
      this.ignoreArtifactDescriptorRepositories = ignoreArtifactDescriptorRepositories;
   }

   /**
    * If true, a dependency report file with dependency information will be created.
    */
   public boolean isCreateDependencyReportFile() {
      return createDependencyReportFile;
   }

   /**
    * Sets if a dependency report file with dependency information will be created.
    */
   public void setCreateDependencyReportFile(boolean createDependencyReportFile) {
      this.createDependencyReportFile = createDependencyReportFile;
   }

   /**
    * Gets if a deployment script file should be created.
    */
   public boolean isCreateDeploymentScriptFile() {
      return createDeploymentScriptFile;
   }

   /**
    * Sets if a deployment script file should be created.
    */
   public void setCreateDeploymentScriptFile(boolean createDeploymentScriptFile) {
      this.createDeploymentScriptFile = createDeploymentScriptFile;
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
    * Factory method to create a new instance of {@code ResolveDependenciesAction}.  Useful for testing.
    */
   protected ResolveDependenciesAction newResolveDependenciesAction() {
      return new ResolveDependenciesAction();
   }

   /**
    * Factory method to create a new instance of {@code ResolveDependenciesAction}.  Useful for testing.
    */
   protected CopyDependencyFilesAction newCopyDependencyFilesAction() {
      return new CopyDependencyFilesAction();
   }

   /**
    * Factory method to create a new instance of {@code CreateDependencyReportAction}.  Useful for testing.
    */
   protected CreateDependencyReportAction newCreateDependencyReportAction() {
      return new CreateDependencyReportAction();
   }

   /**
    * Factory method to create a new instance of {@code RemoveSnapshotsAction}.  Useful for testing.
    */
   protected RemoveSnapshotsAction newRemoveSnapshotsAction() {
      return new RemoveSnapshotsAction();
   }

   /**
    * Factory method to create a new instance of {@code CreateDeploymentScriptAction}.  Useful for testing.
    */
   protected CreateDeploymentScriptAction newCreateDeploymentScriptAction() {
      return new CreateDeploymentScriptAction();
   }

   /**
    * Creates an artifact store for all resolved dependencies.  This data structure makes it easier to generate scripts
    * and reports from the dependency data.
    */
   private ArtifactResultStore createStore(Collection<DependencyResult> dependencyResults) {
      ArtifactResultStore store = new ArtifactResultStore(Paths.get(localRepository.getUrl()),
                                                          outputDirectory.toPath());
      for (DependencyResult dependencyResult : dependencyResults) {
         for (ArtifactResult artifactResult : dependencyResult.getArtifactResults()) {
            Optional<Path> pom = findPom(artifactResult);
            if (pom.isPresent()) {
               store.addResult(artifactResult, pom.get());
            } else {
               String prettyGave = String.format(
                     "%s:%s:%s%s@%s",
                     artifactResult.getArtifact().getGroupId(),
                     artifactResult.getArtifact().getArtifactId(),
                     artifactResult.getArtifact().getVersion(),
                     artifactResult.getArtifact().getClassifier() == null ? "" : ":" + artifactResult.getArtifact()
                           .getClassifier(),
                     artifactResult.getArtifact().getExtension());
               getLogger().warn("POM file not found for {}, artifact will not be included in reports or scripts.",
                                prettyGave);
            }
         }
      }
      store.finish();
      return store;
   }

   /**
    * Finds the POM on disk for the given artifact.
    */
   static Optional<Path> findPom(ArtifactResult artifactResult) {
      Optional<Path> path = Optional.empty();
      File artifact = artifactResult.getArtifact().getFile();
      if (artifact.getParentFile() != null) {
         Path pom = FileUtils.listFiles(artifact.getParentFile(), new String[]{"pom"}, false)
               .stream()
               .map(File::toPath)
               .findAny()
               .orElse(null);
         path = Optional.ofNullable(pom);
      }
      return path;
   }
}
