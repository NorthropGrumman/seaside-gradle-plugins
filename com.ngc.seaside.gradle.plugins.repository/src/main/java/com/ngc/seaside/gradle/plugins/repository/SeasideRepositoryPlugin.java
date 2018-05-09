package com.ngc.seaside.gradle.plugins.repository;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Plugin for creating and configuring Gradle repositories. This plugin provides the
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#NAME} extension of type
 * {@link SeasideRepositoryExtension}. This extension allows you to configure which repositories get created and how.
 * 
 * <p>
 * By default, this plugin will add {@link RepositoryHandler#mavenLocal() maven local} and a remote
 * {@link RepositoryHandler#maven(org.gradle.api.Action) maven} repository with the url taken from the project property
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY}.
 * If {@link MavenPlugin maven} plugin or {@link MavenPublishPlugin maven-publish} plugin is applied to the project,
 * this plugin will also add an upload/publish repository with the url taken from the project property
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_RELEASES_PROPERTY}
 * or
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_SNAPSHOTS_PROPERTY}
 * depending on the project version, along with the username and password set to the project properties taken from
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_USERNAME_PROPERTY}
 * and
 * {@value com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryExtension#DEFAULT_REMOTE_MAVEN_PASSWORD_PROPERTY},
 * respectively.
 * 
 * <p>
 * The following is an example of how to change the default repository configurations:
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
 *    
 *    // maven local will not be added to project or build script repositories
 *    includeMavenLocal = false
 *    
 *    // don't create any project repositories
 *    generateProjectRepositories = false
 *    
 *    // immediately create the repositories
 *    configure()
 * }
 * </pre>
 */
public class SeasideRepositoryPlugin extends AbstractProjectPlugin {

   @Override
   protected void doApply(Project project) {
      SeasideRepositoryExtension extension = project.getExtensions().create(SeasideRepositoryExtension.NAME,
         SeasideRepositoryExtension.class,
         project);

      project.afterEvaluate(__ -> {
         extension.configure();
      });
   }

}
