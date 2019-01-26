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
package com.ngc.seaside.gradle.plugins.distribution;

import com.ngc.seaside.gradle.util.TaskResolver;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SeasideServiceDistributionPluginIT {

   private Project project;
   private SeasideServiceDistributionPlugin plugin;

   @Before
   public void before() {
      project = ProjectBuilder.builder().build();
      plugin = new SeasideServiceDistributionPlugin();
      plugin.apply(project);
   }

   @Test
   public void doesApplyPlugin() {
      TaskResolver resolver = new TaskResolver(project);

      assertNotNull(resolver.findTask("copyResources"));
      assertNotNull(resolver.findTask("copyPlatformBundles"));
      assertNotNull(resolver.findTask("copyThirdPartyBundles"));
      assertNotNull(resolver.findTask("copyBundles"));
      assertNotNull(resolver.findTask("tar"));
      assertNotNull(resolver.findTask("zip"));
      assertNotNull(resolver.findTask("buildDist"));
   }

}
