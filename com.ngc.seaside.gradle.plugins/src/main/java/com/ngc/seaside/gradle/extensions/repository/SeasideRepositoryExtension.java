package com.ngc.seaside.gradle.extensions.repository;

import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin;
import com.ngc.seaside.gradle.util.VersionResolver;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

import java.util.Objects;

import groovy.lang.Closure;

/**
 * Extension for the {@link SeasideRepositoryPlugin}. This extension allows for the customization of repository
 * configuration using {@link RepositoryConfiguration}. These configuration can be set for the consolidated, releases, and
 * snapshosts repositories.
 * 
 * <p>
 * By default, the extension will instruct the plugin to generate repositories for the project. If the
 * {@link MavenPlugin maven} plugin or {@link MavenPublishPlugin maven-publish} is applied, the plugin will by default
 * generate repositories for the respective plugin.
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

   private final Project project;
   
   private boolean generateBuildScriptRepositories = false;
   private boolean generateProjectRepositories = true;
   private boolean generateMavenUploadRepositories = false;
   private boolean generateMavenPublishRepositories = false;
   private boolean includeMavenLocal = true;

   private RepositoryConfiguration consolidatedConfig;
   private RepositoryConfiguration releasesConfig;
   private RepositoryConfiguration snapshotsConfig;

   /**
    * Constructs the extension, setting the default values for the repository configuration.
    * 
    * @param project project
    */
   public SeasideRepositoryExtension(Project project) {
      this.project = project;
      consolidatedConfig = new RepositoryConfiguration();
      releasesConfig = new RepositoryConfiguration();
      snapshotsConfig = new RepositoryConfiguration();

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
   public void setIncludeMavenLocal(boolean include) {
      this.includeMavenLocal = include;
   }

   /**
    * Returns the repository configuration for the consolidated repository.
    * 
    * @return the repository configuration for the consolidated repository
    */
   public RepositoryConfiguration getConsolidatedConfiguration() {
      return this.consolidatedConfig;
   }

   /**
    * Returns the repository configuration for the releases repository.
    * 
    * @return the repository configuration for the releases repository
    */
   public RepositoryConfiguration getReleasesConfiguration() {
      return this.releasesConfig;
   }

   /**
    * Returns the repository configuration for the snapshots repository.
    * 
    * @return the repository configuration for the snapshots repository
    */
   public RepositoryConfiguration getSnapshotsConfiguration() {
      return this.snapshotsConfig;
   }

   /**
    * Returns the repository configuration for the uploading repository. This will be either the
    * {@link #getReleasesConfiguration() releases configuration} or 
    * {@link #getSnapshotsConfiguration() snapshots configuration} depending on the project version.
    * 
    * @return the repository configuration for the uploading repository
    */
   public RepositoryConfiguration getUploadConfiguration() {
      return VersionResolver.isSnapshot(project) ? getSnapshotsConfiguration() : getReleasesConfiguration();
   }
   
   /**
    * Sets the repository configuration for the consolidated repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setConsolidatedConfiguration(RepositoryConfiguration configuration) {
      this.consolidatedConfig = configuration;
      return this;
   }

   /**
    * Sets the repository configuration for the releases repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setReleasesConfiguration(RepositoryConfiguration configuration) {
      Objects.requireNonNull(configuration, "configuration cannot be null");
      this.releasesConfig = configuration;
      return this;
   }

   /**
    * Sets the repository configuration for the snapshots repository.
    * 
    * @param configuration the repository configuration
    * @return this
    */
   public SeasideRepositoryExtension setSnapshotsConfiguration(RepositoryConfiguration configuration) {
      Objects.requireNonNull(configuration, "configuration cannot be null");
      this.snapshotsConfig = configuration;
      return this;
   }

   /**
    * Applies the given action to the consolidated, releases, and snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension allRepositories(Action<RepositoryConfiguration> action) {
      Objects.requireNonNull(action, "action cannot be null");
      applyAction(action, consolidatedConfig, releasesConfig, snapshotsConfig);
      return this;
   }

   /**
    * Applies the given closure to the consolidated, releases, and snapshots repository configuration. The type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension allRepositories(Closure<?> c) {
      Objects.requireNonNull(c, "closure cannot be null");
      applyAction(configuration -> c.call(configuration), consolidatedConfig, releasesConfig, snapshotsConfig);
      return this;
   }

   /**
    * Applies the given action to the releases and snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension uploadRepositories(Action<RepositoryConfiguration> action) {
      Objects.requireNonNull(action, "action cannot be null");
      applyAction(action, releasesConfig, snapshotsConfig);
      return this;
   }

   /**
    * Applies the given closure to the releases and snapshots repository configuration. The type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension uploadRepositories(Closure<?> c) {
      Objects.requireNonNull(c, "closure cannot be null");
      applyAction(configuration -> c.call(configuration), releasesConfig, snapshotsConfig);
      return this;
   }

   /**
    * Applies the given action to the consolidated repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension consolidated(Action<RepositoryConfiguration> action) {
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(consolidatedConfig);
      return this;
   }

   /**
    * Applies the given closure to the consolidated repository configuration. The type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension consolidated(Closure<?> c) {
      Objects.requireNonNull(c, "closure cannot be null");
      c.call(consolidatedConfig);
      return this;
   }

   /**
    * Applies the given action to the releases repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension releases(Action<RepositoryConfiguration> action) {
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(releasesConfig);
      return this;
   }

   /**
    * Applies the given closure to the releases repository configuration. The type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension releases(Closure<?> c) {
      c.call(releasesConfig);
      Objects.requireNonNull(c, "closure cannot be null");
      return this;
   }

   /**
    * Applies the given action to the snapshots repository configuration.
    * 
    * @param action action to execute
    * @return this
    */
   public SeasideRepositoryExtension snapshots(Action<RepositoryConfiguration> action) {
      Objects.requireNonNull(action, "action cannot be null");
      action.execute(snapshotsConfig);
      return this;
   }

   /**
    * Applies the given closure to the snapshots repository configuration. The type of the closure
    * is {@link RepositoryConfiguration}.
    * 
    * @param c closure to call
    * @return this
    */
   public SeasideRepositoryExtension snapshots(Closure<?> c) {
      Objects.requireNonNull(c, "closure cannot be null");
      c.call(snapshotsConfig);
      return this;
   }

   private void applyAction(Action<RepositoryConfiguration> action, RepositoryConfiguration... configuration) {
      RepositoryConfiguration combined = new RepositoryConfiguration() {

         @Override
         public RepositoryConfiguration setName(String repositoryName) {
            for (RepositoryConfiguration d : configuration) {
               d.setName(repositoryName);
            }
            return super.setName(repositoryName);
         }

         @Override
         public RepositoryConfiguration setUrlProperty(String repositoryProperty) {
            for (RepositoryConfiguration d : configuration) {
               d.setUrlProperty(repositoryProperty);
            }
            return super.setUrlProperty(repositoryProperty);
         }

         @Override
         public RepositoryConfiguration setUsernameProperty(String usernameProperty) {
            for (RepositoryConfiguration d : configuration) {
               d.setUsernameProperty(usernameProperty);
            }
            return super.setUsernameProperty(usernameProperty);
         }

         @Override
         public RepositoryConfiguration setPasswordProperty(String passwordProperty) {
            for (RepositoryConfiguration d : configuration) {
               d.setPasswordProperty(passwordProperty);
            }
            return super.setPasswordProperty(passwordProperty);
         }

         @Override
         public RepositoryConfiguration setRequired(boolean required) {
            for (RepositoryConfiguration d : configuration) {
               d.setRequired(required);
            }
            return super.setRequired(required);
         }

         @Override
         public RepositoryConfiguration setAuthenticationRequired(boolean required) {
            for (RepositoryConfiguration d : configuration) {
               d.setAuthenticationRequired(required);
            }
            return super.setAuthenticationRequired(required);
         }

         @Override
         public RepositoryConfiguration required() {
            for (RepositoryConfiguration d : configuration) {
               d.required();
            }
            return super.required();
         }

         @Override
         public RepositoryConfiguration optional() {
            for (RepositoryConfiguration d : configuration) {
               d.optional();
            }
            return super.optional();
         }

      };

      action.execute(combined);
   }

}
