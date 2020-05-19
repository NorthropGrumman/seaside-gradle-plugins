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
package com.ngc.seaside.gradle.plugins.eclipse.updatesite;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeasideEclipseUpdateSitePluginFT {

   private Project project;
   private File projectDir;

   @Before
   public void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
               sourceDirectoryWithTheTestProject(),
               pathToTheDestinationProjectDirectory());
      project = TestingUtilities.createTheTestProjectWith(projectDir);
   }

   @Test
   public void runsGradleBuildWithSuccess() throws Throwable {
      BuildResult result = SeasideGradleRunner.create()
               .withNexusProperties()
               .withProjectDir(projectDir)
               .withPluginClasspath()
               .forwardOutput()
               .withArguments("clean", "build")
               .build();

      TestingUtilities.assertTaskSuccess(result, "service.heiverden", "build");

      String projectPrefix = "com.ngc.seaside.service";
      Path projectBuildDir =
               Paths.get(projectDir.getAbsolutePath(), projectPrefix + ".heiverden", "build", "updatesite");
      Path updateSitePath = projectBuildDir.resolve("com.ngc.seaside.service.heiverden-1.2.3-SNAPSHOT");
      Path featuresPath = updateSitePath.resolve("features");
      Path internalFeature = featuresPath.resolve("com.ngc.seaside.systemdescriptor.feature_1.2.3.SNAPSHOT.jar");

      Path pluginsPath = updateSitePath.resolve("plugins");
      Path customPluginPath = pluginsPath.resolve(projectPrefix + ".helloworld_1.2.3.SNAPSHOT.jar");
      Path eclipsePluginPath = pluginsPath.resolve("org.antlr.runtime_3.2.0.v201101311130.jar");
      Path artifactsPath = updateSitePath.resolve("artifacts.jar");
      Path contentPath = updateSitePath.resolve("content.jar");
      Path updateSiteArchivePath = projectBuildDir.resolve("com.ngc.seaside.service.heiverden-1.2.3-SNAPSHOT.zip");
      Path misVersioned3rdPartyItemPath = pluginsPath.resolve("org.glassfish.javax.json_1.1.0.jar");
      Path blacklistedComponentPath = pluginsPath.resolve("org.apache.commons.collections_3.2.2.jar");

      assertTrue(featuresPath + " directory was not created!", project.file(featuresPath).exists());
      assertTrue(internalFeature + " file was not created!", project.file(internalFeature).exists());
      assertTrue(customPluginPath + " directory was not created!", project.file(customPluginPath).exists());
      assertTrue(eclipsePluginPath + " directory was not created!", project.file(eclipsePluginPath).exists());
      assertTrue(artifactsPath + " directory was not created!", project.file(artifactsPath).exists());
      assertTrue(contentPath + " directory was not created!", project.file(contentPath).exists());
      assertTrue(updateSiteArchivePath + " directory was not created!", project.file(updateSiteArchivePath).exists());
      assertTrue(misVersioned3rdPartyItemPath + " file was not created!",
                 project.file(misVersioned3rdPartyItemPath).exists());
      assertFalse(blacklistedComponentPath + " file should not be created since it was blacklisted!",
                  project.file(blacklistedComponentPath).exists());
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
               "src", "functionalTest", "resources", "sealion-java-hello-world");
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
               "build", "functionalTest", "eclipse", "updatesite", "sealion-java-hello-world");
   }
}
