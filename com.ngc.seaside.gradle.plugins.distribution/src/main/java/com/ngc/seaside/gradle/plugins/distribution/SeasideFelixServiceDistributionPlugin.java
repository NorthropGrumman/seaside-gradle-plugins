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

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.ci.SeasideCiExtension;
import com.ngc.seaside.gradle.plugins.ci.SeasideCiPlugin;
import com.ngc.seaside.gradle.plugins.repository.SeasideRepositoryPlugin;
import com.ngc.seaside.gradle.util.OsgiResolver;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Plugin for creating an OSGi service distribution that can be deployed with Apache Felix.
 *
 * <p>
 * This plugin provides the
 * {@value com.ngc.seaside.gradle.plugins.distribution.SeasideFelixServiceDistributionExtension#NAME} extension
 * of type {@link SeasideFelixServiceDistributionExtension}. This extension allows for adding system properties and
 * program arguments to the start scripts and changing the default {@code config.properties} file.
 *
 * <p>
 * When built, this plugin will produce a distribution zip with the following layout:
 * <ul>
 * <li>bin - contains the start.sh and start.bat scripts</li>
 * <li>bundles - contains all of the bundle jars</li>
 * <li>resources - contains all of the resources found in the project's {@code src/main/resources/runtime} folder</li>
 * <li>platform - contains all of the platform jars necessary for running Apache Felix</li>
 * </ul>
 *
 * <p>
 * This plugin provides the following configurations:
 * <ul>
 * <li>{@value #BUNDLES_CONFIG_NAME} - bundles dependencies. This is the primary configuration used to add bundles
 * to the OSGi framework. You can create another bundle configuration with different settings by adding them to the
 * extension; for example:
 *
 * <pre>
 * apply plugin: 'com.ngc.seaside.distribution.felixservice'
 * configurations {
 *    thirdParty {
 *       transitive = false
 *    }
 * }
 * felixService {
 *    bundleConfiguration 'thirdParty' // bundle configuration already added by default
 * }
 * </pre>
 *
 * When the distribution is being created, the jar dependencies from the bundle configurations will be resolved.
 * Unlike the default way that Gradle handles configurations, this plugin will only copy jars that are OSGi compliant;
 * it will also include all jar versions of a dependency when there are version conflicts - not just one.
 *
 * </li>
 * <li>{@value #CORE_BUNDLES_CONFIG_NAME} - core bundle dependencies. This configuration provides
 * {@link Configuration#defaultDependencies(org.gradle.api.Action) default dependencies}. This means if you explicitly
 * add any dependencies to the core bundle configuration, the default dependencies will not be included. If you want to
 * add more dependencies while including all of the defaults, use the {@value #BUNDLES_CONFIG_NAME} configuration.
 * </li>
 * <li>{@value #PLATFORM_CONFIG_NAME} - dependencies needed for running Apache Felix. This configuration provides
 * {@link Configuration#defaultDependencies(org.gradle.api.Action) default dependencies}. This means if you explicitly
 * add any dependencies to the platform configuration, the default dependencies will not be included. If you want to
 * add more dependencies while including all of the defaults, create a new configuration and have the platform
 * configuration extend it:
 *
 * <pre>
 * apply plugin: 'com.ngc.seaside.distribution.felixservice'
 * configurations {
 *    extraPlatform
 *    platform.extendsFrom extraPlatform
 * }
 * dependencies {
 *    extraPlatform "org.apache.felix:org.apache.felix.new.dependency:$felixVersion"
 * }
 * </pre>
 *
 * </li>
 * </ul>
 */
public class SeasideFelixServiceDistributionPlugin extends AbstractProjectPlugin {

   public static final String PLATFORM_CONFIG_NAME = "platform";
   public static final String BUNDLES_CONFIG_NAME = "bundles";
   public static final String CORE_BUNDLES_CONFIG_NAME = "coreBundles";
   public static final String JVM_PROPERTIES_SCRIPT_IDENTIFIER = "FELIX_JVM_PROPERTIES";
   public static final String PROGRAM_ARGUMENTS_SCRIPT_IDENTIFIER = "FELIX_PROGRAM_ARGUMENTS";
   public static final String BIN_DIRECTORY = "bin";
   public static final String PLATFORM_DIRECTORY = "platform";
   public static final String CONFIG_DIRECTORY = PLATFORM_DIRECTORY + "/configuration";
   public static final String RESOURCES_DIRECTORY = "resources";
   public static final String BUNDLES_DIRECTORY = "bundles";
   public static final String DISTRIBUTION_DIRECTORY = "distribution";
   public static final String RUNTIME_RESOURCES_DIRECTORY = "src/main/resources/runtime";
   public static final List<String> DEFAULT_PLATFORM_DEPENDENCIES = Collections.unmodifiableList(Arrays.asList(
         "org.apache.felix:org.apache.felix.configadmin:1.8.16",
         "org.apache.felix:org.apache.felix.eventadmin:1.5.0",
         "org.apache.felix:org.apache.felix.gogo.command:1.0.2",
         "org.apache.felix:org.apache.felix.gogo.runtime:1.0.8",
         "org.apache.felix:org.apache.felix.gogo.shell:1.0.0",
         "org.apache.felix:org.apache.felix.log:1.0.1",
         "org.apache.felix:org.apache.felix.main:5.6.10",
         "org.apache.felix:org.apache.felix.metatype:1.1.6",
         "org.apache.felix:org.apache.felix.scr:2.0.14",
         "com.ngc.blocs:service.deployment.impl.common.autodeploymentservice:$blocsCoreVersion"));
   public static final List<String> DEFAULT_BUNDLE_DEPENDENCIES = Collections.unmodifiableList(Arrays.asList(
         "com.ngc.blocs:api:$blocsCoreVersion",
         "com.ngc.blocs:file.impl.common.fileutilities:$blocsCoreVersion",
         "com.ngc.blocs:security.impl.common.securityutilities:$blocsCoreVersion",
         "com.ngc.blocs:jaxb.impl.common.jaxbutilities:$blocsCoreVersion",
         "com.ngc.blocs:xml.resource.impl.common.xmlresource:$blocsCoreVersion",
         "com.ngc.blocs:service.log.impl.common.logservice:$blocsCoreVersion",
         "com.ngc.blocs:service.api:$blocsCoreVersion",
         "com.ngc.blocs:service.event.impl.common.eventservice:$blocsCoreVersion",
         "com.ngc.blocs:service.resource.impl.common.resourceservice:$blocsCoreVersion",
         "com.ngc.blocs:notification.impl.common.notificationsupport:$blocsCoreVersion",
         "com.ngc.blocs:properties.resource.impl.common.propertiesresource:$blocsCoreVersion",
         "com.ngc.blocs:service.framework.impl.common.frameworkmgmtservice:$blocsCoreVersion",
         "com.ngc.blocs:component.impl.common.componentutilities:$blocsCoreVersion",
         "com.ngc.blocs:service.notification.impl.common.notificationservice:$blocsCoreVersion",
         "com.ngc.blocs:service.thread.impl.common.threadservice:$blocsCoreVersion"));

   public static final String ZIP_DISTRIBUTION_TASK = "createFelixDistribution";

   /**
    * If a bundle configuration contains a {@link Boolean} attribute with the given name, and it is set to {@code true},
    * the configuration will use its configured dependency resolution strategy and not include duplicate dependencies
    * with differing versions.
    */
   public static final String INCLUDE_CONFLICTING_VERSIONS_ATTRIBUTE_NAME = "includeConflictingVersions";

   /**
    * Used to set executable permissions on the eclipse executable.  Equivalent to unix file permissions: rwxr-xr-x.
    */
   private static final int UNIX_EXECUTABLE_PERMISSIONS = 493;

   private final VersionSelectorScheme versionSelectorscheme;

   @Inject
   public SeasideFelixServiceDistributionPlugin(VersionSelectorScheme versionSelectorscheme) {
      this.versionSelectorscheme = versionSelectorscheme;
   }

   @Override
   protected void doApply(Project project) {
      applyPlugins(project);
      createExtension(project);
      createConfigurations(project);
      addDefaultDependencies(project);
      createTasks(project);
      createArchives(project);
   }

   private void applyPlugins(Project project) {
      project.getPlugins().apply(BasePlugin.class);
      project.getPlugins().apply(MavenPublishPlugin.class);
      // Apply the ci and repo plugin so we can use the populateM2task to collect the dependencies required to build
      // a distribution project.
      project.getPlugins().apply(SeasideRepositoryPlugin.class);
      project.getPlugins().apply(SeasideCiPlugin.class);
   }

   private void createConfigurations(Project project) {
      project.getConfigurations().create(PLATFORM_CONFIG_NAME, config -> {
         config.setTransitive(false);
         config.setDescription("Configuration for dependencies required by the service distribution platform");
      });
      Configuration core = project.getConfigurations().create(CORE_BUNDLES_CONFIG_NAME, config -> {
         config.setTransitive(true);
         config.setDescription("Configuration for core bundle dependencies needed by the service distribution");
      });
      Configuration bundle = project.getConfigurations().create(BUNDLES_CONFIG_NAME, config -> {
         config.setTransitive(true);
         config.setDescription("Configuration for bundle dependencies needed by the service distribution");
         config.extendsFrom(core);
      });
      SeasideFelixServiceDistributionExtension extension =
            project.getExtensions().findByType(SeasideFelixServiceDistributionExtension.class);
      extension.bundleConfiguration(bundle);

      // Setup the populateM2task so it will resolve the configurations created above.  This is because these
      // configurations use default dependencies and we need to explicitly resolve these configurations to get the
      // dependencies.
      SeasideCiExtension seasideCi = project.getExtensions().getByType(SeasideCiExtension.class);
      seasideCi.forceResolutionOf(PLATFORM_CONFIG_NAME);
      seasideCi.forceResolutionOf(core);
      seasideCi.forceResolutionOf(bundle);
   }

   private void createExtension(Project project) {
      project.getExtensions().create(SeasideFelixServiceDistributionExtension.NAME,
                                     SeasideFelixServiceDistributionExtension.class,
                                     project);
   }

   private void addDefaultDependencies(Project project) {
      project.getConfigurations().getByName(PLATFORM_CONFIG_NAME).defaultDependencies(dps -> {
         DependencyHandler h = project.getDependencies();
         for (String dependency : DEFAULT_PLATFORM_DEPENDENCIES) {
            String gav = dependency;
            String version = dependency.substring(dependency.lastIndexOf(':') + 1);
            if (version.charAt(0) == '$') {
               Object property = project.findProperty(version.substring(1));
               if (property == null) {
                  throw new GradleException(version.substring(1) + " property must be set");
               }
               version = property.toString();
               gav = dependency.substring(0, dependency.lastIndexOf(':')) + ':' + version;
            }
            dps.add(h.create(gav));
         }
      });
      project.getConfigurations().getByName(CORE_BUNDLES_CONFIG_NAME).defaultDependencies(dps -> {
         DependencyHandler h = project.getDependencies();

         for (String dependency : DEFAULT_BUNDLE_DEPENDENCIES) {
            String gav = dependency;
            String version = dependency.substring(dependency.lastIndexOf(':') + 1);
            if (version.charAt(0) == '$') {
               Object property = project.findProperty(version.substring(1));
               if (property == null) {
                  throw new GradleException(version.substring(1) + " property must be set");
               }
               version = property.toString();
               gav = dependency.substring(0, dependency.lastIndexOf(':')) + ':' + version;
            }
            dps.add(h.create(gav));
         }
      });
   }

   private void createTasks(Project project) {
      TaskContainer tasks = project.getTasks();
      SeasideFelixServiceDistributionExtension extension = project.getExtensions()
            .getByType(SeasideFelixServiceDistributionExtension.class);

      Action<ResourceCopyTask> taskAction = task -> task.setDestinationDir(task.getTemporaryDir());
      ResourceCopyTask createWindowsScript = tasks.create("createWindowsScript", ResourceCopyTask.class, taskAction);

      ResourceCopyTask createLinuxScript = tasks.create("createLinuxScript", ResourceCopyTask.class, taskAction);

      ResourceCopyTask createConfig = tasks.create("createConfigProperties", ResourceCopyTask.class, taskAction);

      project.afterEvaluate(__ -> {
         String jvmArguments = extension.getJvmArgumentsString();
         String programArguments = extension.getProgramArgumentsString();
         Map<String, String> templateProperties = new LinkedHashMap<>();
         templateProperties.put(JVM_PROPERTIES_SCRIPT_IDENTIFIER, jvmArguments);
         templateProperties.put(PROGRAM_ARGUMENTS_SCRIPT_IDENTIFIER, programArguments);
         File windowsScript = extension.getScripts().getWindowsScript();
         Action<CopySpec> copyAction =
               spec -> spec.expand(templateProperties).eachFile(this::setExecuteBitOnShellScripts);

         if (windowsScript == null) {
            createWindowsScript.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("start.bat")),
                  copyAction);
         } else {
            createWindowsScript.from(windowsScript, copyAction);
         }
         File linuxScript = extension.getScripts().getLinuxScript();
         if (linuxScript == null) {
            createLinuxScript.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY, getClass().getResource("start.sh")),
                  copyAction);
         } else {
            createLinuxScript.from(linuxScript, copyAction);
         }
         File configFile = extension.getConfigFile();
         if (configFile == null) {
            createConfig.fromResource(
                  Collections.singletonMap(ResourceCopyTask.RESOURCE_KEY,
                                           getClass().getResource("config.properties")));
         } else {
            createConfig.from(configFile);
         }
      });

      // Warnings for Maven tasks being deprecated
      tasks.maybeCreate("install")
               .dependsOn("publishToMavenLocal")
               .doFirst(install -> {
                  install.getLogger().warn("The install task will be deprecated with the Gradle maven plugin.");
               });

      tasks.maybeCreate("upload")
               .dependsOn("publish")
               .doFirst(upload -> {
                  upload.getLogger().warn("The upload task will be deprecated with the Gradle maven plugin.");
               });
      
      tasks.maybeCreate("uploadArchives")
               .dependsOn("publish")
               .doFirst(uploadArchives -> {
                  uploadArchives.getLogger().warn("The upload archives task will be deprecated with the Gradle maven plugin.");
               });

      tasks.create(ZIP_DISTRIBUTION_TASK, Zip.class, task -> {
         task.setDescription("Creates the distribution zip");
         task.dependsOn(createWindowsScript, createLinuxScript, createConfig);
         tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(task);
         task.setDestinationDir(new File(project.getBuildDir(), DISTRIBUTION_DIRECTORY));
         task.setIncludeEmptyDirs(true);
         task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

         project.afterEvaluate(__ -> {
            File skeleton = task.getTemporaryDir();
            createDistributionSkeleton(skeleton.toPath());
            task.setArchiveName(extension.getDistributionName());
            task.from(skeleton);
            task.from(createWindowsScript.getDestinationDir(), spec -> spec.into(BIN_DIRECTORY));
            task.from(createLinuxScript.getDestinationDir(), spec -> spec.into(BIN_DIRECTORY));
            task.from(createConfig.getDestinationDir(), spec -> spec.into(CONFIG_DIRECTORY));
            task.from(project.getConfigurations().getByName(PLATFORM_CONFIG_NAME),
                      spec -> spec.into(PLATFORM_DIRECTORY));
            task.from(project.file(RUNTIME_RESOURCES_DIRECTORY), spec -> spec.into(RESOURCES_DIRECTORY));
            for (Configuration configuration : extension.getBundleConfigurations()) {
               copyBundleConfigurations(task, configuration);
            }
         });

         // Unzip the distribution
         task.doLast(___ -> {
            project.copy(spec -> {
               String archiveName = task.getArchiveName();
               archiveName = archiveName.substring(0, archiveName.length() - task.getExtension().length() - 1);
               spec.from(project.zipTree(task.getArchivePath()));
               spec.into(project.getBuildDir() + "/" + DISTRIBUTION_DIRECTORY + "/" + archiveName);
            });
         });
      });
   }

   /**
    * Creates the folder structure of the distribution. This is necessary so that if, for example, there are no
    * resources to be copied, the resources folder is still created.
    *
    * @return the temporary folder
    */
   private void createDistributionSkeleton(Path tempDirectory) {
      try {
         Files.createDirectories(tempDirectory.resolve(BIN_DIRECTORY));
         Files.createDirectories(tempDirectory.resolve(PLATFORM_DIRECTORY));
         Files.createDirectories(tempDirectory.resolve(RESOURCES_DIRECTORY));
         Files.createDirectories(tempDirectory.resolve(CONFIG_DIRECTORY));
         Files.createDirectories(tempDirectory.resolve(BUNDLES_DIRECTORY));
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /**
    * Adds copy commands to the given task for the given configuration. This method ensures that only OSGi-compliant
    * files are copied. It ensures that correctly-versioned dependencies are copied. The names of the copied files
    * are also deconflicted by adding the group id to the filenames.
    *
    * @param task          copy task
    * @param configuration configuration
    */
   private void copyBundleConfigurations(AbstractCopyTask task, Configuration configuration) {
      Project project = task.getProject();
      SeasideFelixServiceDistributionExtension extension = project.getExtensions()
            .getByType(SeasideFelixServiceDistributionExtension.class);

      OsgiResolver osgiResolver = new OsgiResolver(project, versionSelectorscheme);
      osgiResolver.resolveAllVersions(configuration, (identifier, file) -> {
         Optional<String> symbolicName = OsgiResolver.getOsgiSymbolicName(file);
         if (symbolicName.isPresent() && !extension.getBlacklist().get().contains(identifier.getDisplayName())) {
            String version = OsgiResolver.getOsgiVersion(file).get();
            String filename = symbolicName.get() + "_" + version;
            task.from(file, spec -> {
               spec.into(BUNDLES_DIRECTORY);
               spec.rename(__ -> filename + "." + FilenameUtils.getExtension(file.getName()));
            });
         } else if (!symbolicName.isPresent()) {
            project.getLogger().info("Excluding file {} from dependency '{}': not an OSGi bundle",
                                     file,
                                     identifier.getDisplayName());
         } else {
            project.getLogger().info("Excluding file {} from dependency '{}': component is blacklisted",
                                     file,
                                     identifier.getDisplayName());
         }
      });
      task.getInputs().file(configuration);
   }

   private void createArchives(Project project) {
      Task zipDistribution = project.getTasks().getByName(ZIP_DISTRIBUTION_TASK);
      PluginContainer plugins = project.getPlugins();
      project.artifacts(handler -> handler.add(Dependency.ARCHIVES_CONFIGURATION, zipDistribution));
      plugins.withType(MavenPublishPlugin.class, plugin -> {
         project.getExtensions().configure(PublishingExtension.class, convention -> {
            convention.publications(publications -> {
               publications.create("mavenDistribution",
                                   MavenPublication.class,
                                   publication -> publication.artifact(zipDistribution));
            });
         });

      });
   }

   private void setExecuteBitOnShellScripts(FileCopyDetails f) {
      if (f.getName().endsWith(".sh")) {
         f.setMode(UNIX_EXECUTABLE_PERMISSIONS);
      }
   }
}
