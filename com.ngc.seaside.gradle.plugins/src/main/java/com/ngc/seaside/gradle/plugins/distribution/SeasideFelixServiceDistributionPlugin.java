package com.ngc.seaside.gradle.plugins.distribution;

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin;
import com.ngc.seaside.gradle.extensions.distribution.SeasideFelixServiceDistributionExtension;
import com.ngc.seaside.gradle.tasks.distribution.SimpleTemplateTask;

import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Plugin for creating an OSGi service distribution that can be deployed with Apache Felix.
 * 
 * <p>
 * This plugin provides the
 * {@value com.ngc.seaside.gradle.extensions.distribution.SeasideFelixServiceDistributionExtension#NAME} extension
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
 * This plugin provides two configurations:
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
   public static final String BLOCS_CORE_VERSION_NAME = "blocsCoreVersion";
   public static final String JVM_PROPERTIES_SCRIPT_IDENTIFIER = "__FELIX_JVM_PROPERTIES__";
   public static final String PROGRAM_ARGUMENTS_SCRIPT_IDENTIFIER = "__FELIX_PROGRAM_ARGUMENTS__";
   public static final String BIN_DIRECTORY = "bin";
   public static final String PLATFORM_DIRECTORY = "platform";
   public static final String CONFIG_DIRECTORY = PLATFORM_DIRECTORY + "/configuration";
   public static final String RESOURCES_DIRECTORY = "resources";
   public static final String BUNDLES_DIRECTORY = "bundles";
   public static final String DISTRIBUTION_DIRECTORY = "distribution";
   public static final String RUNTIME_RESOURCES_DIRECTORY = "src/main/resources/runtime";
   public static final List<String> DEFAULT_PLATFORM_DEPENDENCIES = Collections.unmodifiableList(Arrays.asList(
      "org.apache.felix:org.apache.felix.configadmin:1.8.16",
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
   public static final List<String> EXCLUDED_BUNDLES = Collections.unmodifiableList(Arrays.asList(
      "org.osgi:org.osgi.core",
      "org.osgi:org.osgi.enterprise"));

   @Override
   protected void doApply(Project project) {
      applyPlugins(project);
      createExtension(project);
      createConfigurations(project);
      addDefaultDependencies(project);
      createTasks(project);
   }

   private void applyPlugins(Project project) {
      project.getPlugins().apply(BasePlugin.class);
      project.getPlugins().apply(MavenPlugin.class);
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
      SeasideFelixServiceDistributionExtension extension = project.getExtensions()
                                                                  .findByType(
                                                                     SeasideFelixServiceDistributionExtension.class);
      extension.bundleConfiguration(bundle);

      project.afterEvaluate(__ -> {
         // Exclude certain bundles
         for (Configuration configuration : extension.getBundleConfigurations()) {
            for (String excludedBundle : EXCLUDED_BUNDLES) {
               Map<String, String> properties = new LinkedHashMap<>();
               String[] parts = excludedBundle.split(":");
               properties.put(ExcludeRule.GROUP_KEY, parts[0]);
               properties.put(ExcludeRule.MODULE_KEY, parts[1]);
               configuration.exclude(properties);
            }
         }
      });
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
      SeasideFelixServiceDistributionExtension extension = project.getExtensions().findByType(
         SeasideFelixServiceDistributionExtension.class);

      SimpleTemplateTask createWindowsScript = tasks.create("createWindowsScript", SimpleTemplateTask.class, task -> {
         task.setDestination(new File(project.getBuildDir(), "tmp/distribution/start.bat"));
         project.afterEvaluate(__ -> {
            String jvmArguments = extension.getJvmArgumentsString();
            String programArguments = extension.getProgramArgumentsString();
            Map<String, String> templateProperties = new LinkedHashMap<>();
            templateProperties.put(JVM_PROPERTIES_SCRIPT_IDENTIFIER, jvmArguments);
            templateProperties.put(PROGRAM_ARGUMENTS_SCRIPT_IDENTIFIER, programArguments);
            task.setTemplateProperties(templateProperties);
            File windowsScript = extension.getScripts().getWindowsScript();
            if (windowsScript == null) {
               task.setTemplate(getClass().getResource("start.bat"));
            } else {
               task.setTemplate(windowsScript);
            }
         });
      });

      SimpleTemplateTask createLinuxScript = tasks.create("createLinuxScript", SimpleTemplateTask.class, task -> {
         task.setDestination(new File(project.getBuildDir(), "tmp/distribution/start.sh"));
         project.afterEvaluate(__ -> {
            String jvmArguments = extension.getJvmArgumentsString();
            String programArguments = extension.getProgramArgumentsString();
            Map<String, String> templateProperties = new LinkedHashMap<>();
            templateProperties.put(JVM_PROPERTIES_SCRIPT_IDENTIFIER, jvmArguments);
            templateProperties.put(PROGRAM_ARGUMENTS_SCRIPT_IDENTIFIER, programArguments);
            task.setTemplateProperties(templateProperties);
            File linuxScript = extension.getScripts().getLinuxScript();
            if (linuxScript == null) {
               task.setTemplate(getClass().getResource("start.sh"));
            } else {
               task.setTemplate(linuxScript);
            }
         });
      });

      SimpleTemplateTask createConfig = tasks.create("createConfigProperties", SimpleTemplateTask.class, task -> {
         task.setDestination(new File(project.getBuildDir(), "tmp/distribution/config.properties"));
         project.afterEvaluate(__ -> {
            File configFile = extension.getConfigFile();
            if (configFile == null) {
               task.setTemplate(getClass().getResource("config.properties"));
            } else {
               task.setTemplate(configFile);
            }
         });
      });

      tasks.create("createDistribution", Zip.class, task -> {
         task.setDescription("Creates the distribution zip");
         task.dependsOn(createWindowsScript, createLinuxScript, createConfig);
         tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME).dependsOn(task);
         task.setDestinationDir(new File(project.getBuildDir(), DISTRIBUTION_DIRECTORY));
         task.setIncludeEmptyDirs(true);

         project.afterEvaluate(__ -> {
            File skeleton = createDistributionSkeleton();
            task.setArchiveName(extension.getDistributionName());
            task.from(skeleton);
            task.from(createWindowsScript.getDestination(), spec -> spec.into(BIN_DIRECTORY));
            task.from(createLinuxScript.getDestination(), spec -> spec.into(BIN_DIRECTORY));
            task.from(project.getConfigurations().getByName(PLATFORM_CONFIG_NAME),
               spec -> spec.into(PLATFORM_DIRECTORY));
            task.from(createConfig.getDestination(), spec -> spec.into(CONFIG_DIRECTORY));
            task.from(project.file(RUNTIME_RESOURCES_DIRECTORY), spec -> spec.into(RESOURCES_DIRECTORY));
            for (Configuration configuration : extension.getBundleConfigurations()) {
               Set<ResolvedArtifact> resolvedArtifacts = configuration.getResolvedConfiguration()
                                                                      .getResolvedArtifacts();
               for (ResolvedArtifact artifact : resolvedArtifacts) {
                  if (isOsgiArtifact(artifact)) {
                     task.from(artifact.getFile(), spec -> {
                        spec.into(BUNDLES_DIRECTORY);
                        spec.rename(filename -> bundleFilename(artifact, filename));
                     });
                  } else {
                     ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
                     project.getLogger().info("Excluding '{}:{}:{}': not an OSGi bundle",
                        id.getGroup(),
                        id.getName(),
                        id.getVersion());
                  }
               }
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
   private File createDistributionSkeleton() {
      try {
         Path skeleton = Files.createTempDirectory(null);
         Files.createDirectories(skeleton.resolve(BIN_DIRECTORY));
         Files.createDirectories(skeleton.resolve(PLATFORM_DIRECTORY));
         Files.createDirectories(skeleton.resolve(RESOURCES_DIRECTORY));
         Files.createDirectories(skeleton.resolve(CONFIG_DIRECTORY));
         Files.createDirectories(skeleton.resolve(BUNDLES_DIRECTORY));
         return skeleton.toFile();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /**
    * Renames the bundle file to ensure that the group id is included in the filename.
    * 
    * @param artifact bundle artifact
    * @param filename artifact filename
    * @return the renamed bundle filename
    */
   private String bundleFilename(ResolvedArtifact artifact, String filename) {
      StringBuilder name = new StringBuilder();
      ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
      name.append(id.getGroup()).append('.').append(id.getName()).append('-').append(id.getVersion());
      if (artifact.getClassifier() != null) {
         name.append('-').append(artifact.getClassifier());
      }
      name.append('.').append(artifact.getExtension());
      return name.toString();
   }

   /**
    * Returns {@code true} if the given artifact represents an OSGi compliant bundle.
    * 
    * @param artifact artifact
    * @return {@code true} if the given artifact represents an OSGi compliant bundle
    */
   private boolean isOsgiArtifact(ResolvedArtifact artifact) {
      File file = artifact.getFile();
      try (ZipFile zipFile = new ZipFile(file)) {
         ZipEntry entry = zipFile.stream()
                                 .filter(e -> !e.isDirectory())
                                 .filter(e -> e.getName().equals("META-INF/MANIFEST.MF"))
                                 .findAny()
                                 .orElseThrow(() -> new ZipException());
         String content = IOUtils.toString(zipFile.getInputStream(entry), Charset.defaultCharset());
         Pattern osgiPattern = Pattern.compile("^Bundle-SymbolicName:", Pattern.MULTILINE);
         return osgiPattern.matcher(content).find();
      } catch (ZipException e) {
         return false;
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

}
