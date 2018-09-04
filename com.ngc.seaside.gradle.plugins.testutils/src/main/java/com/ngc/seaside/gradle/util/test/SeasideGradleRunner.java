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
package com.ngc.seaside.gradle.util.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.InvalidPluginMetadataException;
import org.gradle.testkit.runner.InvalidRunnerConfigurationException;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.gradle.testkit.runner.UnexpectedBuildSuccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SeasideGradleRunner extends GradleRunner {

   private final GradleRunner runner;

   private SeasideGradleRunner() {
      runner = GradleRunner.create();
      runner.withArguments("-S");
   }

   public static SeasideGradleRunner create() {
      return new SeasideGradleRunner();
   }

   public SeasideGradleRunner withPropertiesFromGradleHome() {
      Path gradleUserHome = Paths.get(System.getenv("GRADLE_USER_HOME"));
      if (gradleUserHome == null) {
         gradleUserHome = Paths.get(System.getProperty("user.home"), ".gradle");
      }
      if (!Files.isDirectory(gradleUserHome)) {
         throw new IllegalStateException("could not find Gradle user home directory! Expected to find a directory in "
                                               + gradleUserHome.toAbsolutePath());
      }
      Path gradleProperties = gradleUserHome.resolve("gradle.properties");
      if (!Files.isRegularFile(gradleProperties)) {
         throw new IllegalStateException("expected to find a regular file at " + gradleProperties.toAbsolutePath());
      }
      Properties properties = new Properties();
      try (InputStream is = Files.newInputStream(gradleProperties)) {
         properties.load(is);
      } catch (IOException e) {
         throw new RuntimeException(e.getMessage(), e);
      }
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
         String key = entry.getKey().toString();
         String value = entry.getValue().toString();
         if (key.startsWith("systemProp.")) {
            key = key.substring("systemProp.".length());
            withArguments(String.format("-D%s=%s", key, value));
         } else {
            withArguments(String.format("-P%s=%s", key, value));
         }
      }
      return this;
   }

   public SeasideGradleRunner withNexusProperties() {
      String value = System.getProperty("nexusConsolidated");
      if (value == null) {
         throw new IllegalStateException("nexusConsolidated property cannot be found");
      }
      String nexusSnapshots = System.getProperty("nexusSnapshots", "test");
      String nexusUsername = System.getProperty("nexusUsername", "test");
      String nexusPassword = System.getProperty("nexusPassword", "test");
      this.withArguments(
            "-PnexusConsolidated=" + value,
            "-PnexusReleases=test",
            "-PnexusSnapshots=" + nexusSnapshots,
            "-PnexusUsername=" + nexusUsername,
            "-PnexusPassword=" + nexusPassword);
      String[] properties = {"javax.net.ssl.trustStore",
                             "http.proxyHost", "http.proxyPort", "http.nonProxyHosts", "https.proxyHost",
                             "https.proxyPort",
                             "https.nonProxyHosts", "sonar.host.url"};
      for (String propertyKey : properties) {
         String propertyValue = System.getProperty(propertyKey);
         if (propertyValue != null) {
            withArguments("-D" + propertyKey + "=" + propertyValue);
         }
      }
      return this;
   }

   @Override
   public SeasideGradleRunner withGradleVersion(String s) {
      runner.withGradleVersion(s);
      return this;
   }

   @Override
   public SeasideGradleRunner withGradleInstallation(File file) {
      runner.withGradleInstallation(file);
      return this;
   }

   @Override
   public SeasideGradleRunner withGradleDistribution(URI uri) {
      runner.withGradleDistribution(uri);
      return this;
   }

   @Override
   public SeasideGradleRunner withTestKitDir(File file) {
      runner.withTestKitDir(file);
      return this;
   }

   @Override
   public File getProjectDir() {
      return runner.getProjectDir();
   }

   @Override
   public SeasideGradleRunner withProjectDir(File file) {
      runner.withProjectDir(file);
      return this;
   }

   @Override
   public List<String> getArguments() {
      return runner.getArguments();
   }

   @Override
   public SeasideGradleRunner withArguments(List<String> list) {
      List<String> arguments = new ArrayList<>(runner.getArguments());
      arguments.addAll(list);
      runner.withArguments(arguments);
      return this;
   }

   @Override
   public SeasideGradleRunner withArguments(String... strings) {
      return withArguments(Arrays.asList(strings));
   }

   @Override
   public List<? extends File> getPluginClasspath() {
      return runner.getPluginClasspath();
   }

   @Override
   public SeasideGradleRunner withPluginClasspath() throws InvalidPluginMetadataException {
      runner.withPluginClasspath();
      return this;
   }

   @Override
   public SeasideGradleRunner withPluginClasspath(Iterable<? extends File> iterable) {
      runner.withPluginClasspath(iterable);
      return this;
   }

   @Override
   public boolean isDebug() {
      return runner.isDebug();
   }

   @Override
   public SeasideGradleRunner withDebug(boolean b) {
      runner.withDebug(b);
      return this;
   }

   @Override
   public SeasideGradleRunner forwardStdOutput(Writer writer) {
      runner.forwardStdOutput(writer);
      return this;
   }

   @Override
   public SeasideGradleRunner forwardStdError(Writer writer) {
      runner.forwardStdError(writer);
      return this;
   }

   @Override
   public SeasideGradleRunner forwardOutput() {
      runner.forwardOutput();
      return this;
   }

   @Override
   public BuildResult build() throws InvalidRunnerConfigurationException, UnexpectedBuildFailure {
      return runner.build();
   }

   @Override
   public BuildResult buildAndFail() throws InvalidRunnerConfigurationException, UnexpectedBuildSuccess {
      return runner.buildAndFail();
   }
}
