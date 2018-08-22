package com.ngc.seaside.gradle.plugins.distribution;

import static org.junit.Assert.assertNotNull;

import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin;
import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin;

public class SeasideFelixServiceDistributionPluginTest {
   
   private Project project;

   @Before
   public void setUp() throws Exception {
      project = ProjectBuilder.builder().build();
      project.getPlugins().apply(SeasideFelixServiceDistributionPlugin.class);
   }

   @Test
   public void testAppliedPlugins() { 
      assertNotNull(project.getPlugins().getPlugin(BasePlugin.class));
      assertNotNull(project.getPlugins().getPlugin(MavenPublishPlugin.class));
      assertNotNull(project.getPlugins().getPlugin(SeasideRepositoryPlugin.class));
      assertNotNull(project.getPlugins().getPlugin(SeasideCiPlugin.class));
   }

}
