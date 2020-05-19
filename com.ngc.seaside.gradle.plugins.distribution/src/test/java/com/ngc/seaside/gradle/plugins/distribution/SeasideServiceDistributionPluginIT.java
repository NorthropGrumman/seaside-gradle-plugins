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
