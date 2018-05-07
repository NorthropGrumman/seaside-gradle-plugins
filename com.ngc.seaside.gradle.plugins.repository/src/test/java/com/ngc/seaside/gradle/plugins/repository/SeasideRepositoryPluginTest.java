package com.ngc.seaside.gradle.plugins.repository;

import static org.junit.Assert.assertEquals;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;
public class SeasideRepositoryPluginTest {

   private Project project;
   
   @Before
   public void setup() {
      project = ProjectBuilder.builder().build();
      project.getExtensions().add(SeasideRepositoryExtension.DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY, "test");
      project.getPlugins().apply(SeasideRepositoryPlugin.class);
   }
   
   @Test
   public void test() {
      ((DefaultProject) project).evaluate();
      assertEquals(2, project.getRepositories().size());
      assertEquals(ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME, project.getRepositories().get(0).getName());
      assertEquals(SeasideRepositoryExtension.DEFAULT_REMOTE_MAVEN_CONSOLIDATED_NAME, project.getRepositories().get(1).getName());
   }
}
