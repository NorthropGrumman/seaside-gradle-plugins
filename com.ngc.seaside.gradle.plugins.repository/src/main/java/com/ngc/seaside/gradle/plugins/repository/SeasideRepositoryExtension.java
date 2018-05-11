package com.ngc.seaside.gradle.plugins.repository;

import com.ngc.seaside.gradle.util.Versions;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.plugins.MavenRepositoryHandlerConvention;
import org.gradle.api.plugins.PluginInstantiationException;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.Upload;

import java.util.Objects;

import groovy.lang.Closure;

/**
 * Extension for the {@link SeasideRepositoryPlugin}. This extension allows for the customization of repository
 * configuration using {@link RepositoryConfiguration}. These configuration can be set for the consolidated, releases, and
 * snapshosts repositories.
 * 
 * <p>
 * By default, the extension will instruct the plugin to generate the maven local and consolidated repositories for the
 * project. If the {@link MavenPlugin maven} plugin or {@link MavenPublishPlugin maven-publish} plugin is applied and
 * the corresponding upload/publish task will run, the plugin will by default generate repositories for the respective
 * plugin.
 */
public class SeasideRepositoryExtension {

   public static final String NAME = "seasideRepository";

   public static final String DEFAULT_REMOTE_MAVEN_CONSOLIDATED_NAME = "NexusConsolidated";
   public static final String DEFAULT_REMOTE_MAVEN_RELEASES_NAME = "NexusReleases";
   public static final String DEFAULT_REMOTE_MAVEN_SNAPSHOTS_NAME = "NexusSnapshots";
   public static final String DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY = "nexusConsolidated";
   public static final String DEFAULT_REMOTE_MAVEN_RELEASES_PROPERTY = "nexusReleases";
   public static final String DEFAULT_REMOTE_MAVEN_SNAPSHOTS_PROPERTY = "nexusSnapshots";
   public static final String DEFAULT_REMOTE_MAVEN_USERNAME_PROPERTY = "nexusUsername";
   public static final String DEFAULT_REMOTE_MAVEN_PASSWORD_PROPERTY = "nexusPassword";

   private final static String MISSING_PROPERTY_ERROR_MESSAGE = "the property '%s' is not set!  Please ensure this property is set.  These type of properties"
      + " can be set in $GRADLE_USER_HOME/gradle.properties.  Note that $GRADLE_USER_HOME is not necessarily"
      + " the directory where Gradle is installed.  If $GRADLE_USER_HOME is not set, gradle.properties can"
      + " usually be found in $USER/.gradle/.  You can check which properties are set by running"
      + " 'gradle properties'.";

   private final Project project;
   private boolean configured = false;

   private boolean generateBuildScriptRepositories = false;
   private boolean generateProjectRepositories = true;
   private boolean generateMavenUploadRepositories = false;
   private boolean generateMavenPublishRepositories = false;
   private boolean includeMavenLocal = true;
   private boolean automaticallyResolveUploadRequirements = true;

   private final RepositoryConfiguration consolidatedConfig;
   private final RepositoryConfiguration releasesConfig;
   private final RepositoryConfiguration snapshotsConfig;

   /**
    * Constructs the extension, setting the default values for the repository configuration.
    * 
    * @param project project
    */
   public SeasideRepositoryExtension(Project project) {
      this.project = project;
      consolidatedConfig = new DefaultRepositoryConfiguration();
      releasesConfig = new DefaultRepositoryConfiguration();
      snapshotsConfig = new DefaultRepositoryConfiguration();

      consolidatedConfig.setName(DEFAULT_REMOTE_MAVEN_CONSOLIDATED_NAME);
      consolidatedConfig.setUrlProperty(DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY);
      consolidatedConfig.setRequired(true);

      releasesConfig.setName(DEFAULT_REMOTE_MAVEN_RELEASES_NAME);
      releasesConfig.setUrlProperty(DEFAULT_REMOTE_MAVEN_RELEASES_PROPERTY);

      snapshotsConfig.setName(DEFAULT_REMOTE_MAVEN_SNAPSHOTS_NAME);
      snapshotsConfig.setUrlProperty(DEFAULT_REMOTE_MAVEN_SNAPSHOTS_PROPERTY);

      allRepositories(configuration -> {
         configuration.setUsernameProperty(DEFAULT_REMOTE_MAVEN_USERNAME_PROPERTY);
         configuration.setPasswordProperty(DEFAULT_REMOTE_MAVEN_PASSWORD_PROPERTY);
      });

      uploadRepositories(configuration -> {
         configuration.setRequired(false);
         configuration.setAuthenticationRequired(true);
      });
      if (project.getPlugins().hasPlugin(MavenPlugin.class)) {
         generateMavenUploadRepositories = true;
      }
      if (project.getPlugins().hasPlugin(MavenPublishPlugin.class)) {
         generateMavenPublishRepositories = true;
      }

      project.getGradle().getTaskGraph().whenReady(graph -> {
         if (automaticallyResolveUploadRequirements) {
            boolean mavenUpload = graph.hasTask(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
               && project.getPlugins().hasPlugin(MavenPlugin.class);
            boolean mavenPublish = graph.hasTask(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
               && project.getPlugins().hasPlugin(MavenPlugin.class);
            if (mavenUpload || mavenPublish) {
               RepositoryConfiguration uploadConfig = getUploadConfiguration();
               if (!uploadConfig.isRequired()) {
                  RepositoryConfiguration newUploadConfig = new DefaultRepositoryConfiguration(uploadConfig);
                  newUploadConfig.setRequired(true);
                  checkRepositoryConfigurations(newUploadConfig);
                  if (mavenUpload) {
                     configureMavenUploadRepositories(newUploadConfig);
                  }
                  if (mavenPublish) {
                     configureMavenPublishRepositories(newUploadConfig);
                  }
               }
            }
         }
      });
   }

   /**
    * Configures the repositories for the project. This will automatically be done after the project is evaluated,
    * but can be called explicitly to configure the repositories before the project is finished with its evaluation.
    */
   public void configure() {
      if (configured) {
         return;
      }
      RepositoryConfiguration consolidatedConfig = getConsolidatedConfiguration();
      RepositoryConfiguration uploadConfig = new DefaultRepositoryConfiguration(getUploadConfiguration());
      if (isGenerateMavenUploadRepositories() || isGenerateMavenPublishRepositories()) {
         uploadConfig.setRequired(true);
      }

      checkRepositoryConfigurations(consolidatedConfig, releasesConfig, snapshotsConfig, uploadConfig);

      if (isGenerateBuildScriptRepositories()) {
         configureBuildScriptRepositories(consolidatedConfig);
      }
      if (isGenerateProjectRepositories()) {
         configureProjectRepositories(consolidatedConfig);
      }
      if (isGenerateMavenUploadRepositories()) {
         configureMavenUploadRepositories(uploadConfig);
      }
      if (isGenerateMavenPublishRepositories()) {
         configureMavenPublishRepositories(uploadConfig);
      }
      configured = true;
   }

   /**
    * Returns whether or not the plugin should generate repositories for the project's build script.
    * 
    * @return whether or not the plugin should generate repositories for the project's build script
    */
   public boolean isGenerateBuildScriptRepositories() {
      return generateBuildScriptRepositories;
   }

   /**
    * Sets whether or not the plugin should generate repositories for the project's build script
    * 
    * @param generateBuildScriptRepositories if the plugin should generate repositories for the project's build script
    * @return this
    */
   public SeasideRepositoryExtension setGenerateBuildScriptRepositories(boolean generateBuildScriptRepositories) {
      checkNotConfigured();
      this.generateBuildScriptRepositories = generateBuildScriptRepositories;
      return this;
   }

   /**
    * Returns whether or not the plugin should generate repositories for the project.
    * 
    * @return whether or not the plugin should generate repositories for the project
    */
   public boolean isGenerateProjectRepositories() {
      return generateProjectRepositories;
   }

   /**
    * Sets whether or not the plugin should generate repositories for the project.
    * 
    * @param generateProjectRepositories if the plugin should generate repositories for the project
    * @return this
    */
   public SeasideRepositoryExtension setGenerateProjectRepositories(boolean generateProjectRepositories) {
      checkNotConfigured();
      this.generateProjectRepositories = generateProjectRepositories;
      return this;
   }

   /**
    * Returns whether or not the plugin should generate repositories for the project's maven upload repositories.
    * 
    * @return whether or not the plugin should generate repositories for the project's maven upload repositories
    */
   public boolean isGenerateMavenUploadRepositories() {
      return generateMavenUploadRepositories;
   }

   /**
    * Sets whether or not the plugin should generate repositories for the project's maven upload repositories.
    * 
    * @param generateMavenUploadRepositories if the plugin should generate repositories for the project's maven
    *           upload repositories
    * @return this
    */
   public SeasideRepositoryExtension setGenerateMavenUploadRepositories(boolean generateMavenUploadRepositories) {
      checkNotConfigured();
      this.generateMavenUploadRepositories = generateMavenUploadRepositories;
      return this;
   }

   /**
    * Returns whether or not the plugin should generate repositories for the project's maven publish repositories.
    * 
    * @return whether or not the plugin should generate repositories for the project's maven publish repositories
    */
   public boolean isGenerateMavenPublishRepositories() {
      return generateMavenPublishRepositories;
   }

   /**
    * Sets whether or not the plugin should generate repositories for the project's maven publish repositories.
    * 
    * @param generateMavenPublishRepositories if the plugin should generate repositories for the project's maven
    *           publish repositories
    * @return this
    */
   public SeasideRepositoryExtension setGenerateMavenPublishRepositories(boolean generateMavenPublishRepositories) {
      checkNotConfigured();
      this.generateMavenPublishRepositories = generateMavenPublishRepositories;
      return this;
   }

   /**
    * Returns whether or not the plugin should include maven local as a repository when creating repositories.
    * 
    * @return whether or not the plugin should include maven local as a repository when creating repositories
    */
   public boolean isIncludeMavenLocal() {
      return includeMavenLocal;
   }

   /**
    * Sets whether or not the plugin should include maven local as a repository when creating repositories
    * 
    * @param include whether or not the plugin should include maven local as a repository when creating repositories
    */
   public SeasideRepositoryExtension setIncludeMavenLocal(boolean include) {
      checkNotConfigured();
      this.includeMavenLocal = include;
      return this;
   }

   /**
    * Sets whether or not the plugin should automatically resolve whether upload repository configurations are required
    * or not. This means that the plugin will check whether or not the {@link MavenPlugin maven} plugin or
    * {@link MavenPublishPlugin maven-publish} plugin have been applied and their corresponding upload/publish task will
    * be executed. If the check passes, then the plugin will require either the snapshots or releases configuration
    * properties (depending on the version) and will create the corresponding repository.
    * 
    * @return whether or not the plugin should automatically resolve the upload repository configurations
    */
   public boolean isAutomaticallyResolveUploadRequirements() {
      return automaticallyResolveUploadRequirements;
   }

   /**
    * Sets whether or not the plugin should automatically resolve whether upload repository configurations are required
    * or not.
    * 
    * @see #isAutomaticallyResolveUploadRequirements()
    * @param conditional whether or not the plugin should automatically resolve the upload repository configurations
    * @return this
    */
   public SeasideRepositoryExtension setAutomaticallyResolveUploadRequirements(
            boolean conditional) {
      this.automaticallyResolveUploadRequirements = conditional;
      return this;
   }

   /**
    * Returns the repository configuration for the consolidated repository.
    * 
    * @return the repository configuration for the consolidated repository
    */
   public RepositoryConfiguration getConsolidatedConfiguration() {
      return new RepositoryConfigurationWrapper(this::checkNotConfigured, consolidatedConfig);
   }

   /**
    * Returns the repository configuration for the releases repository.
    * 
    * @return the repository configuration for the releases repository
    */
   public RepositoryConfiguration getReleasesConfiguration() {
      return new RepositoryConfigurationWrapper(this::checkNotConfigured, releasesConfig);
   }

   /**
    * Returns the repository configuration for the snapshots repository.
    * 
    * @return the repository configuration for the snapshots repository
    */
   public RepositoryConfiguration getSnapshotsConfiguration() {
      return new RepositoryConfigurationWrapper(this::checkNotConfigured, snapshotsConfig);
   }

   /**
    * Returns the repository configuration for the uploading repository. This will be either the
    * {@link #getReleasesConfiguration() releases configuration} or
    * {@link #getSnapshotsConfiguration() snapshots configuration} depending on the project version.
    * 
    * @return the repository configuration for the uploading repository
    */
   public RepositoryConfiguration getUploadConfiguration() {
      return Versions.isSnapshot(project) ? getSnapshotsConfiguration() : getReleasesConfiguration();
   }

   /**
    * Sets the repository configuration for the consolidated repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setConsolidatedConfiguration(RepositoryConfiguration configuration) {
      checkNotConfigured();
      this.consolidatedConfig.from(configuration);
      return this;
   }

   /**
    * Sets the repository configuration for the releases repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setReleasesConfiguration(RepositoryConfiguration configuration) {
      checkNotConfigured();
      this.releasesConfig.from(configuration);
      return this;
   }

   /**
    * Sets the repository configuration for the snapshots repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setSnapshotsConfiguration(RepositoryConfiguration configuration) {
      checkNotConfigured();
      this.snapshotsConfig.from(configuration);
      return this;
   }

   /**
    * Applies the given action to the consolidated, releases, and snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension allRepositories(Action<RepositoryConfiguration> action) {
      checkNotConfigured();
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(
         new RepositoryConfigurationWrapper(this::checkNotConfigured, consolidatedConfig, releasesConfig,
            snapshotsConfig));
      return this;
   }

   /**
    * Applies the given closure to the consolidated, releases, and snapshots repository configuration. The argument type
    * of the closure is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension allRepositories(Closure<?> c) {
      checkNotConfigured();
      Objects.requireNonNull(c, "closure cannot be null");
      return allRepositories(config -> c.call(config));
   }

   /**
    * Applies the given action to the releases and snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension uploadRepositories(Action<RepositoryConfiguration> action) {
      checkNotConfigured();
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(new RepositoryConfigurationWrapper(this::checkNotConfigured, releasesConfig, snapshotsConfig));
      return this;
   }

   /**
    * Applies the given closure to the releases and snapshots repository configuration. The argument type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension uploadRepositories(Closure<?> c) {
      checkNotConfigured();
      Objects.requireNonNull(c, "closure cannot be null");
      return uploadRepositories(config -> c.call(config));
   }

   /**
    * Applies the given action to the consolidated repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension consolidated(Action<RepositoryConfiguration> action) {
      checkNotConfigured();
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(new RepositoryConfigurationWrapper(this::checkNotConfigured, consolidatedConfig));
      return this;
   }

   /**
    * Applies the given closure to the consolidated repository configuration. The argument type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension consolidated(Closure<?> c) {
      checkNotConfigured();
      Objects.requireNonNull(c, "closure cannot be null");
      consolidated(config -> c.call(config));
      return this;
   }

   /**
    * Applies the given action to the releases repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension releases(Action<RepositoryConfiguration> action) {
      checkNotConfigured();
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(new RepositoryConfigurationWrapper(this::checkNotConfigured, releasesConfig));
      return this;
   }

   /**
    * Applies the given closure to the releases repository configuration. The argument type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension releases(Closure<?> c) {
      checkNotConfigured();
      Objects.requireNonNull(c, "closure cannot be null");
      releases(config -> c.call(config));
      return this;
   }

   /**
    * Applies the given action to the snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension snapshots(Action<RepositoryConfiguration> action) {
      checkNotConfigured();
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(new RepositoryConfigurationWrapper(this::checkNotConfigured, snapshotsConfig));
      return this;
   }

   /**
    * Applies the given closure to the snapshots repository configuration. The argument type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension snapshots(Closure<?> c) {
      checkNotConfigured();
      Objects.requireNonNull(c, "closure cannot be null");
      snapshots(config -> c.call(config));
      return this;
   }

   private void configureBuildScriptRepositories(RepositoryConfiguration config) {
      project.getLogger().info("Creating consolidated repositories for build script");
      RepositoryHandler handler = project.getBuildscript().getRepositories();
      createRepositories(project, getConsolidatedConfiguration(), handler, isIncludeMavenLocal());
   }

   private void configureProjectRepositories(RepositoryConfiguration config) {
      project.getLogger().info("Creating consolidated repositories for project");
      RepositoryHandler handler = project.getRepositories();
      createRepositories(project, config, handler, isIncludeMavenLocal());
   }

   private void configureMavenUploadRepositories(RepositoryConfiguration uploadConfig) {
      project.getPlugins().apply(MavenPlugin.class);
      project.getLogger().info("Creating consolidated repositories for maven upload tasks");
      Upload uploadArchives = (Upload) project.getTasks().getByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME);
      MavenRepositoryHandlerConvention convention = ((HasConvention) uploadArchives.getRepositories()).getConvention()
                                                                                                      .getPlugin(
                                                                                                         MavenRepositoryHandlerConvention.class);
      convention.mavenDeployer(deployer -> {
         Object repo = getRemoteRepository(uploadConfig, deployer.getClass().getClassLoader());
         if (repo != null) {
            if (Versions.isSnapshot(project)) {
               deployer.setSnapshotRepository(repo);
            } else {
               deployer.setRepository(repo);
            }
         }
      });
   }

   private Object getRemoteRepository(RepositoryConfiguration config, ClassLoader loader) {
      String repositoryName = config.getName();
      String url = getProperty(project, config.getUrlProperty());
      String username = getProperty(project, config.getUsernameProperty());
      String password = getProperty(project, config.getPasswordProperty());
      if (url == null) {
         return null;
      }
      project.getLogger().info("Setting up remote repository {} with url {} and username {}", repositoryName, url, username);
      try {
         Class<?> remoteRepositoryClass = loader.loadClass("org.apache.maven.artifact.ant.RemoteRepository");
         Class<?> authenticationClass = loader.loadClass("org.apache.maven.artifact.ant.Authentication");
         Class<?> serverClass = loader.loadClass("org.apache.maven.settings.Server");

         Object repo = remoteRepositoryClass.getConstructor().newInstance();

         if (repositoryName != null) {
            remoteRepositoryClass.getMethod("setId", String.class).invoke(repo, repositoryName);
         }
         remoteRepositoryClass.getMethod("setUrl", String.class).invoke(repo, url);
         if (username != null && password != null) {
            Object server = serverClass.getConstructor().newInstance();
            serverClass.getMethod("setUsername", String.class).invoke(server, username);
            serverClass.getMethod("setPassword", String.class).invoke(server, password);
            Object authentication = authenticationClass.getConstructor(serverClass).newInstance(server);
            remoteRepositoryClass.getMethod("addAuthentication", authenticationClass).invoke(repo, authentication);
         }
         return repo;
      } catch (Exception e) {
         throw new IllegalStateException(e);
      }
   }

   private void configureMavenPublishRepositories(RepositoryConfiguration uploadConfig) {
      project.getPlugins().apply(MavenPublishPlugin.class);
      project.getLogger().info("Creating consolidated repositories for maven publish");
      project.getExtensions().configure(PublishingExtension.class, convention -> {
         convention.repositories(handler -> createRepositories(project, uploadConfig, handler, false));
      });
   }

   private void createRepositories(Project project, RepositoryConfiguration config, RepositoryHandler handler,
            boolean includeMavenLocal) {
      if (includeMavenLocal) {
         handler.mavenLocal();
      }
      String repositoryName = config.getName();
      String url = getProperty(project, config.getUrlProperty());
      String username = getProperty(project, config.getUsernameProperty());
      String password = getProperty(project, config.getPasswordProperty());
      if (url != null) {
         handler.maven(repo -> {
            repo.setUrl(url);
            if (repositoryName != null) {
               repo.setName(repositoryName);
            }
            if (config.isAuthenticationRequired() && username != null && password != null) {
               repo.credentials(credentials -> {
                  credentials.setUsername(username);
                  credentials.setPassword(password);
               });
            }
         });
      }
   }

   private void checkRepositoryConfigurations(RepositoryConfiguration... configurations) {
      for (RepositoryConfiguration config : configurations) {
         if (config.isRequired()) {
            requireProperty("url property", config.getUrlProperty());
            if (config.isAuthenticationRequired()) {
               requireProperty("username property", config.getUsernameProperty());
               requireProperty("password proeprty", config.getPasswordProperty());
            }
         }
      }
   }

   private void requireProperty(String propertyName, String property) {
      if (property == null) {
         throw new ProjectConfigurationException(propertyName + " cannot be null", null);
      }
      if (!project.hasProperty(property)) {
         throw new ProjectConfigurationException(String.format(MISSING_PROPERTY_ERROR_MESSAGE, property),
            null);
      }
   }

   private String getProperty(Project project, String key) {
      if (key == null) {
         return null;
      }
      Object value = project.findProperty(key);
      if (value == null) {
         return null;
      }
      return value.toString();
   }

   private void checkNotConfigured() {
      if (configured) {
         throw new PluginInstantiationException(
            "The com.ngc.seaside.repository plugin has already configured the repositories; " + NAME
               + " extension cannot not be modified");
      }
   }

}
