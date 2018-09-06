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
package com.ngc.seaside.gradle.plugins.parent;

import com.ngc.seaside.gradle.util.TaskResolver;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class SeasideRootParentPluginIT {

   private File projectDir;
   private Project project;
   private SeasideRootParentPlugin plugin;

   @Before
   public void before() throws Throwable {
      File source = Paths.get("src/integrationTest/resources/sealion-java-hello-world").toFile();
      Path targetPath = Paths.get("build/integrationTest/resources/parent/com.ngc.example.parent");
      projectDir = Files.createDirectories(targetPath).toFile();
      FileUtils.copyDirectory(source, projectDir);

      project = ProjectBuilder.builder().withProjectDir(projectDir).build();

      plugin = new SeasideRootParentPlugin();

      setRequiredProjectProperties(project);
      plugin.apply(project);
   }

   @Test
   public void doesApplyPlugin() {
      TaskResolver resolver = new TaskResolver(project);
      assertNotNull(resolver.findTask(SeasideRootParentPlugin.LICENSE_CHECK_GRADLE_SCRIPTS_TASK_NAME));
      assertNotNull(resolver.findTask(SeasideRootParentPlugin.LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME));
   }

   private static void setRequiredProjectProperties(Project project) {
      String test = "test";
      project.getExtensions().add("nexusReleases", test);
      project.getExtensions().add("nexusUsername", test);
      project.getExtensions().add("nexusPassword", test);
      project.getExtensions().add("nexusSnapshots", test);
      project.getExtensions().add("nexusConsolidated", test);
   }
}
