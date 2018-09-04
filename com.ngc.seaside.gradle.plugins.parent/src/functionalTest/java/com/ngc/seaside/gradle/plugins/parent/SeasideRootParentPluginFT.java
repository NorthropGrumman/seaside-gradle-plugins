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
            .withArguments("clean", SeasideRootParentPlugin.LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME)
            .build();

      assertEquals(TaskOutcome.valueOf("SUCCESS"),
                   result.task(":" + SeasideRootParentPlugin.LICENSE_FORMAT_GRADLE_SCRIPTS_TASK_NAME).getOutcome());
   }
}
