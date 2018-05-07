package com.ngc.seaside.gradle.plugins.repository;

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin;
import com.ngc.seaside.gradle.extensions.repository.RepositoryConfiguration;
import com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension;
import com.ngc.seaside.gradle.util.VersionResolver;

import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.Upload;

/**
 * Plugin for creating and configuring Gradle repositories. This plugin provides the
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#NAME} extension of type
 * {@link SeasideRepositoryExtension}. This extension allows you to configure which repositories get created and how.
 * 
 * <p> By default, this plugin will add {@link RepositoryHandler#mavenLocal() maven local} and a remote
 * {@link RepositoryHandler#maven(org.gradle.api.Action) maven} repository with the url taken from the project property
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY}.
 * If {@link MavenPlugin} or {@link MavenPublishPlugin} are applied to the project, this plugin will also add a
 * repository to their extensions with the url taken from the project property
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_RELEASES_PROPERTY}
 * or
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_SNAPSHOTS_PROPERTY}
 * depending on the project version, along with the username and password set to the project properties taken from
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_USERNAME_PROPERTY}
 * and
 * {@value com.ngc.seaside.gradle.extensions.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_PASSWORD_PROPERTY},
 * respectively.
 * 
 * <p> The following is an example of how to change the default repository configurations:
 * 
 * <pre>
 * seasideRepository {
 *    allRepositories {
 *       // Change the property keys for username and password
 *       usernameProperty = 'artifactoryUsername'
 *       passwordProperty = 'artifactoryPassword'
 *    }
 *    consolidated {
 *       // username and password will now be required and configured for the consolidated repository
 *       authenticationRequired()
 *    }
 *    uploadRepositories {
 *       // Both releases and shapshots should be pushed to the same repository
 *       urlProperty = 'nexusUploads'
 *    }
 *    // maven local will not be added to project or build script repositories
 *    includeMavenLocal = false
 *    // don't create any project repositories
 *    generateProjectRepositories = false
 * }
 * </pre>
 */
public class SeasideRepositoryPlugin extends AbstractProjectPlugin {

   private final static String MISSING_PROPERTY_ERROR_MESSAGE = 
            "the property '%s' is not set!  Please ensure this property is set.  These type of properties"
      + " can be set in $GRADLE_USER_HOME/gradle.properties.  Note that $GRADLE_USER_HOME is not necessarily"
      + " the directory where Gradle is installed.  If $GRADLE_USER_HOME is not set, gradle.properties can"
      + " usually be found in $USER/.gradle/.  You can check which properties are set by running"
      + " 'gradle properties'.";

   @Override
   protected void doApply(Project project) {
      createExtension(project);
      project.afterEvaluate(__ -> {
         SeasideRepositoryExtension extension = (SeasideRepositoryExtension) project.getExtensions()
                                                                                    .getByName(SeasideRepositoryExtension.NAME);

         boolean isSnapshot = VersionResolver.isSnapshot(project);
         RepositoryConfiguration consolidatedConfig = extension.getConsolidatedConfiguration();
         RepositoryConfiguration uploadConfig = isSnapshot ? extension.getSnapshotsConfiguration()
                  : extension.getReleasesConfiguration();

         project.getGradle().getTaskGraph().whenReady(graph -> {
            if (graph.hasTask(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME)
               || graph.hasTask(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)) {
               uploadConfig.setRequired(true);
               checkRepositoryConfigurations(project, extension);
            }
         });

         checkRepositoryConfigurations(project, extension);

         if (extension.isGenerateBuildScriptRepositories()) {
            project.getLogger().info("Creating consolidated repositories for build script");
            RepositoryHandler handler = project.getBuildscript().getRepositories();
            createRepositories(project, consolidatedConfig, handler, extension.isIncludeMavenLocal());
         }
         if (extension.isGenerateProjectRepositories()) {
            project.getLogger().info("Creating consolidated repositories for project");
            RepositoryHandler handler = project.getRepositories();
            createRepositories(project, consolidatedConfig, handler, extension.isIncludeMavenLocal());
         }
         if (extension.isGenerateMavenUploadRepositories()) {
            project.getPlugins().apply(MavenPlugin.class);
            project.getLogger().info("Creating consolidated repositories for maven upload tasks");
            Upload uploadArchives = (Upload) project.getTasks().getByName(BasePlugin.UPLOAD_ARCHIVES_TASK_NAME);
            uploadArchives.repositories(handler -> createRepositories(project, uploadConfig, handler, false));
         }
         if (extension.isGenerateMavenPublishRepositories()) {
            project.getPlugins().apply(MavenPublishPlugin.class);
            project.getLogger().info("Creating consolidated repositories for maven publish");
            project.getExtensions().configure(PublishingExtension.class, convention -> {
               convention.repositories(handler -> createRepositories(project, uploadConfig, handler, false));
            });
         }

      });

   }

   private void createExtension(Project project) {
      project.getExtensions().create(SeasideRepositoryExtension.NAME, SeasideRepositoryExtension.class, project);
   }

   private void checkRepositoryConfigurations(Project project, SeasideRepositoryExtension extension) {
      RepositoryConfiguration[] configurations = { extension.getConsolidatedConfiguration(),
               extension.getReleasesConfiguration(),
               extension.getSnapshotsConfiguration() };

      for (RepositoryConfiguration config : configurations) {
         if (config.isRequired()) {
            requireProperty(project, "url property", config.getUrlProperty());
            if (config.isAuthenticationRequired()) {
               requireProperty(project, "username property", config.getUsernameProperty());
               requireProperty(project, "password proeprty", config.getPasswordProperty());
            }
         }
      }
   }

   private void requireProperty(Project project, String propertyName, String property) {
      if (property == null) {
         throw new ProjectConfigurationException(propertyName + " cannot be null", null);
      }
      if (!project.hasProperty(property)) {
         throw new ProjectConfigurationException(String.format(MISSING_PROPERTY_ERROR_MESSAGE, property),
            null);
      }
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

}
