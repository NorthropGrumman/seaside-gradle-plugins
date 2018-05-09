package com.ngc.seaside.gradle.util.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.InvalidPluginMetadataException;
import org.gradle.testkit.runner.InvalidRunnerConfigurationException;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.gradle.testkit.runner.UnexpectedBuildSuccess;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeasideGradleRunner extends GradleRunner {

   private final GradleRunner runner;

   private SeasideGradleRunner() {
      runner = GradleRunner.create();
      runner.withArguments("-S");
   }

   public static SeasideGradleRunner create() {
      return new SeasideGradleRunner();
   }

   public SeasideGradleRunner withNexusProperties() {
      String value = System.getProperty("nexusConsolidated");
      if (value == null) {
         throw new IllegalStateException("nexusConsolidated property cannot be found");
      }
      this.withArguments(
            "-PnexusConsolidated=" + value,
            "-PnexusReleases=test",
            "-PnexusSnapshots=test",
            "-PnexusUsername=test",
            "-PnexusPassword=test");
      String[] properties = {"javax.net.ssl.trustStore", "http.proxyHost", "http.proxyPort", "http.nonProxyHosts",
                             "https.proxyHost", "https.proxyPort", "https.nonProxyHosts"};
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