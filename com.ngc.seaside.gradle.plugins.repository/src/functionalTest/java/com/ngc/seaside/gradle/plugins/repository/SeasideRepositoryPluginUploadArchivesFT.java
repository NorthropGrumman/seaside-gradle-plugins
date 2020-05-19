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
package com.ngc.seaside.gradle.plugins.repository;

import static org.junit.Assert.fail;

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner;
import com.ngc.seaside.gradle.util.test.TestingUtilities;

import org.gradle.testkit.runner.BuildResult;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class SeasideRepositoryPluginUploadArchivesFT {
   private File projectDir;

   @Before
   public void before() {
      projectDir = TestingUtilities.setUpTheTestProjectDirectory(
         sourceDirectoryWithTheTestProject(),
         pathToTheDestinationProjectDirectory());
   }

   @Test
   public void canUpload() {
      SeasideGradleRunner runner = SeasideGradleRunner.create()
                                                      .withNexusProperties()
                                                      .withProjectDir(projectDir)
                                                      .withPluginClasspath()
                                                      .forwardOutput()
                                                      .withArguments("clean",
                                                         "build",
                                                         "install",
                                                         "uploadArchives",
                                                         "-S",
                                                         "--info");

      try {
         BuildResult result = runner.build();
         TestingUtilities.assertTaskSuccess(result, null, "uploadArchives");
      } catch (Exception e) {
         e.printStackTrace(new PasswordHidingWriter(System.err));
         fail();
      }
   }

   private static File sourceDirectoryWithTheTestProject() {
      return TestingUtilities.turnListIntoPath(
         "src",
         "functionalTest",
         "resources",
         "upload-test");
   }

   private static File pathToTheDestinationProjectDirectory() {
      return TestingUtilities.turnListIntoPath(
         "build",
         "functionalTest",
         "repository",
         "upload-test");
   }
}
