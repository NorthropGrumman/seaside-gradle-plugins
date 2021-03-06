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

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class SeasideRootParentPluginFT {

   private File projectDir;
   private Project project;

   @Before
   public void before() throws Throwable {
      File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile();
      Path targetPath = Paths.get("build/functionalTest/parent/sealion-java-hello-world");
      projectDir = Files.createDirectories(targetPath).toFile();
      FileUtils.copyDirectory(source, projectDir);

      project = ProjectBuilder.builder().withProjectDir(projectDir).build();

      // Skip tests that cannot connect to sonarqube
      Properties properties = new Properties();
      try {
         properties.load(Files.newInputStream(targetPath.resolve("gradle.properties")));
      } catch (Exception e) {
         // ignore
      }
   }

   @Test
   public void testDoesRunLicenseFormat() {
      BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
            .withNexusProperties()
            .withPluginClasspath()
            .forwardOutput()
            .withArguments("clean", SeasideRootParentPlugin.LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME, "--rerun-tasks")
            .build();

      assertEquals(TaskOutcome.valueOf("SUCCESS"),
                   result.task(":" + SeasideRootParentPlugin.LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME).getOutcome());
   }
}
