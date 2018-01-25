package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import groovy.lang.Closure;

import org.eclipse.aether.repository.RepositoryPolicy;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

import java.io.File;

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
    * Contains all the resolved artifacts thus far.
    */
   private ArtifactResultStore store;

   /**
    * The user configured output directory to populate.
    */
   private File outputDirectory;

   /**
    * The file that will be written that contains the dependency info in CSV form.
    */
   private File dependencyInfoCsvFile;

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
      ResolveDependenciesAction resolveDependencies = new ResolveDependenciesAction();
      CopyDependencyFilesAction copyDependencyFiles = new CopyDependencyFilesAction();

      resolveDependencies.validate(this);
      copyDependencyFiles.validate(this);

      resolveDependencies.execute(this);

      copyDependencyFiles.setDependencyResults(resolveDependencies.getDependencyResults());
      copyDependencyFiles.execute(this);
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
    * Gets the file that will be written that contains the dependency info in CSV form.
    */
   @OutputFile
   @Optional
   public File getDependencyInfoCsvFile() {
      return dependencyInfoCsvFile;
   }

   /**
    * Sets file that will be written that contains the dependency info in CSV form.
    */
   public void setDependencyInfoCsvFile(File dependencyInfoCsvFile) {
      this.dependencyInfoCsvFile = Preconditions.checkNotNull(dependencyInfoCsvFile,
                                                              "dependencyInfoCsvFile may not be null!");
   }

   /**
    * Sets file that will be written that contains the dependency info in CSV form.  This method allows a user to
    * specify the output CSV file as a command line option.
    */
   @Option(option = "dependencyInfoCsvFile",
         description = "The CSV file to output which contains the dependency information.")
   public void setDependencyInfoCsvFile(String dependencyInfoCsvFile) {
      Preconditions.checkNotNull(dependencyInfoCsvFile, "dependencyInfoCsvFile may not be null!");
      Preconditions.checkArgument(!dependencyInfoCsvFile.trim().isEmpty(), "dependencyInfoCsvFile may not be null!");
      setDependencyInfoCsvFile(new File(dependencyInfoCsvFile));
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
}
