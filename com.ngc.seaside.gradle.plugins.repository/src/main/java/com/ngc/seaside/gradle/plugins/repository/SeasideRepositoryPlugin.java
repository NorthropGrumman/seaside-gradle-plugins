/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
