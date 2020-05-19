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
