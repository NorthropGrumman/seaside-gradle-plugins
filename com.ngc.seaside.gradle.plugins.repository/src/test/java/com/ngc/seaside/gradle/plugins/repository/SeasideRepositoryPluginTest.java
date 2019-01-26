/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactRepositoryContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;
public class SeasideRepositoryPluginTest {

   private Project project;
   private SeasideRepositoryExtension extension;
   
   @Before
   public void setup() {
      project = ProjectBuilder.builder().build();
      project.getExtensions().add(SeasideRepositoryExtension.DEFAULT_REMOTE_MAVEN_CONSOLIDATED_PROPERTY, "test");
      project.getPlugins().apply(SeasideRepositoryPlugin.class);
      extension = project.getExtensions().findByType(SeasideRepositoryExtension.class);
      assertNotNull(extension);
   }
   
   @Test
   public void testRepositoryPlugin() {
      extension.configure();
      assertEquals(2, project.getRepositories().size());
      assertEquals(ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME, project.getRepositories().get(0).getName());
      assertEquals(SeasideRepositoryExtension.DEFAULT_REMOTE_MAVEN_CONSOLIDATED_NAME, project.getRepositories().get(1).getName());
   }
   
   @Test
   public void testRepositoryPluginWithExtension() {
      extension.setIncludeMavenLocal(false);
      extension.getConsolidatedConfiguration().setName("test");
      extension.setIncludeMavenLocal(true);
      extension.configure();
      assertEquals(2, project.getRepositories().size());
      assertEquals(ArtifactRepositoryContainer.DEFAULT_MAVEN_LOCAL_REPO_NAME, project.getRepositories().get(0).getName());
      assertEquals("test", project.getRepositories().get(1).getName());
   }
}
