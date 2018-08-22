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
      assertNotNull(resolver.findTask("install"));
      assertNotNull(resolver.findTask("upload"));
      assertNotNull(resolver.findTask("uploadArchives"));
   }

}
