package com.ngc.seaside.gradle.plugins.eclipse.updatesite;

import com.ngc.seaside.gradle.api.AbstractProjectPlugin;
import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipseExtension;
import com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin;
import com.ngc.seaside.gradle.plugins.eclipse.p2.MirrorP2Task;
import com.ngc.seaside.gradle.plugins.eclipse.p2.SeasideEclipseP2Plugin;
import com.ngc.seaside.gradle.plugins.eclipse.updatesite.category.EclipseCategory;
import com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature.EclipseFeature;
import com.ngc.seaside.gradle.util.OsgiResolver;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import static com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin.ECLIPSE_TASK_GROUP_NAME;
import static com.ngc.seaside.gradle.plugins.eclipse.BaseEclipsePlugin.UNZIP_ECLIPSE_TASK_NAME;

/**
 * Plugin used for building an Eclipse update site.
 *
 * <p>
 * This plugin creates the {@value #ECLIPSE_UPDATE_SITE_EXTENSION_NAME} extension that uses
 * {@link SeasideEclipseUpdateSiteExtension}. Projects that use this plugin must set the eclipse version and
 * download url using the base eclipse plugin {@link BaseEclipsePlugin#EXTENSION_NAME extension}.
 *
 * <p>
 * This plugin provides the {@value #PLUGINS_CONFIGURATION_NAME} configuration for adding plugin dependencies to the
 * update site. This plugin will include all OSGi compliant dependencies and transitive dependencies including
 * dependencies that only differ by version.
 * Example:
 * <pre>
 * apply plugin: 'com.ngc.seaside.eclipse.updatesite'
 * eclipseDistribution {
 *    linuxVersion = 'eclipse-dsl-photon-R-linux-gtk-x86_64'
 *    windowsVersion = 'eclipse-dsl-photon-R-win32-x86_64'
 *    linuxDownloadUrl = ...
 *    windowsDownloadUrl = ...
 *    enablePluginsRepository() // creates a directory repository pointing to the downloaded eclipse distribution
 * }
 * dependencies {
 *    plugin project(':project.name.ide')
 *    plugin "com.ngc.seaside:systemdescriptor.model.impl.xtext:$version"
 *    plugins name: 'org.eclipse.xtext.lib_2.12.0.v20170518-0757'
 * }
 * </pre>
 * 
 * <p>
 * Features can be added using {@link SeasideEclipseUpdateSiteExtension#feature(Action)}.
 * Example:
 * <pre>
 * apply plugin: 'com.ngc.seaside.eclipse.updatesite'
 * eclipseDistribution { ... }
 * eclipseUpdateSite {
 *    feature {
 *       id = 'com.ngc.seaside.systemdescriptor.feature'
 *       label = 'JellyFish SystemDescriptor DSL'
 *       version = project.version
 *       providerName = 'Northrop Grumman Corporation'
 *       description {
 *          url = 'http://www.systemdescriptor.seaside.ngc.com/description'
 *          text = 'This is the JellyFish System Descriptor Domain Specific Language Eclipse plugin.'
 *       }
 *       copyright {
 *          url = 'http://www.systemdescriptor.seaside.ngc.com/copyright'
 *          text = project.resources.text.fromFile(project.file('src/main/resources/license.txt')).asString()
 *       }
 *       license {
 *          url = 'http://www.systemdescriptor.seaside.ngc.com/license'
 *          text = copyright.text
 *       }
 *       plugin {
 *          id = 'com.ngc.seaside.systemdescriptor'
 *          version = '0.0.0'
 *          unpack = false
 *       }
 *    }
 * }
 * </pre>
 * 
 * <p>
 * Features can be categorized using {@link SeasideEclipseUpdateSiteExtension#category(Action)}.
 * Example:
 * <pre>
 * apply plugin: 'com.ngc.seaside.eclipse.updatesite'
 * eclipseDistribution { ... }
 * eclipseUpdateSite {
 *    def feature1 = feature { ... }
 *    def feature2 = feature { ... }
 *    category {
 *       name = 'system_descriptor_category_id'
 *       label = 'System Descriptor Plugin'
 *       description = 'Eclipse Plugin for the Seaside System Descriptor'
 *       features feature1, feature2
 *    }
 * }
 * </pre>
 * 
 * @see BaseEclipsePlugin
 * @see SeasideEclipseP2Plugin
 * @see SeasideEclipseUpdateSiteExtension
 */
public class SeasideEclipseUpdateSitePlugin extends AbstractProjectPlugin {

   /**
    * The eclipse update site extension name.
    */
   public static final String ECLIPSE_UPDATE_SITE_EXTENSION_NAME = "eclipseUpdateSite";

   /**
    * The name of the task for copying features to the update site.
    */
   public static final String ECLIPSE_CREATE_FEATURES_TASK_NAME = "copyFeatures";

   /**
    * The name of the task for copying the custom plugins to the update site.
    */
   public static final String COPY_PLUGINS_TASK_NAME = "copyPlugins";

   /**
    * The name of the task for creating the eclipse metadata for the update site.
    */
   public static final String CREATE_METADATA_TASK_NAME = "createMetadata";

   /**
    * The name of the task for publishing the eclipse category for the update site.
    */
   public static final String PUBLISH_CATEGORY_TASK_NAME = "publishCategory";

   /**
    * The name of the task for creating the update site zip file.
    */
   public static final String CREATE_UPDATE_SITE_ZIP_TASK_NAME = "createZip";

   /**
    * The name of the plugin configuration.
    */
   public static final String PLUGINS_CONFIGURATION_NAME = "plugin";

   private final VersionSelectorScheme versionSelectorscheme;

   @Inject
   public SeasideEclipseUpdateSitePlugin(VersionSelectorScheme versionSelectorscheme) {
      this.versionSelectorscheme = versionSelectorscheme;
   }

   @Override
   public void doApply(Project project) {
      applyPlugins(project);

      project.getExtensions().create(ECLIPSE_UPDATE_SITE_EXTENSION_NAME,
               SeasideEclipseUpdateSiteExtension.class, project);

      ConfigurationContainer configurations = project.getConfigurations();
      configurations.create(PLUGINS_CONFIGURATION_NAME);

      createTasks(project);
   }

   private static void applyPlugins(Project project) {
      project.getPlugins().apply(BasePlugin.class);
      project.getPlugins().apply(BaseEclipsePlugin.class);
   }

   private void createTasks(Project project) {
      SeasideEclipseUpdateSiteExtension extension = (SeasideEclipseUpdateSiteExtension) project.getExtensions()
               .getByName(ECLIPSE_UPDATE_SITE_EXTENSION_NAME);
      BaseEclipseExtension baseExtension =
               (BaseEclipseExtension) project.getExtensions().getByName(BaseEclipsePlugin.EXTENSION_NAME);
      TaskContainer tasks = project.getTasks();
      Configuration plugins = project.getConfigurations().getByName(PLUGINS_CONFIGURATION_NAME);
      tasks.create(ECLIPSE_CREATE_FEATURES_TASK_NAME, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Creates the feature jars for the update site");
         task.dependsOn(tasks.withType(MirrorP2Task.class));
         task.getInputs().property("features", extension.getFeatures());
         Provider<Directory> featuresDirectory = extension.getUpdateSiteDirectory().map(dir -> dir.dir("features"));
         task.getOutputs().dir(featuresDirectory);
         task.doLast(__ -> {
            for (EclipseFeature feature : extension.getFeatures().getOrElse(Collections.emptySet())) {
               File tempDirectory = task.getTemporaryDir();
               File xml = project.file(tempDirectory + "/feature.xml");
               try (Writer writer = Files.newBufferedWriter(xml.toPath())) {
                  feature.toXml(writer);
               } catch (IOException e) {
                  throw new TaskExecutionException(task, e);
               }
               String name = feature.getId() + "-" + feature.getVersion() + ".jar";
               String jarName = BaseEclipsePlugin.getValidEclipseName(name)
                        .orElseThrow(() -> new IllegalStateException("Invalid feature: " + name));
               Path jarFile = featuresDirectory.get().getAsFile().toPath().resolve(jarName);
               URI fileUri = jarFile.toUri();
               URI zipUri;
               try {
                  zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);
               } catch (URISyntaxException e) {
                  throw new TaskExecutionException(task, e);
               }
               try (FileSystem zipfs = FileSystems.newFileSystem(zipUri,
                        Collections.singletonMap("create", String.valueOf(Files.notExists(jarFile))))) {
                  Files.copy(xml.toPath(), zipfs.getPath("feature.xml"));
               } catch (IOException e) {
                  throw new TaskExecutionException(task, e);
               }
            }
         });
      });
      Task resolvePlugins = tasks.create("resolvePluginConfiguration", task -> {
         task.getInputs().files(plugins);
         task.dependsOn(UNZIP_ECLIPSE_TASK_NAME);
         task.doFirst(__ -> plugins.getResolvedConfiguration());
      });
      tasks.create(COPY_PLUGINS_TASK_NAME, Copy.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Copies the plugins to the update site");
         task.dependsOn(tasks.withType(MirrorP2Task.class), resolvePlugins);
         Provider<Directory> pluginsDirectory = extension.getUpdateSiteDirectory().map(dir -> dir.dir("plugins"));
         task.doFirst(__ -> pluginsDirectory.get().getAsFile().mkdirs());
         OsgiResolver osgiResolver = new OsgiResolver(project, versionSelectorscheme);
         osgiResolver.resolveAllVersions(plugins, (identifier, file) -> {
            Optional<String> name = BaseEclipsePlugin.getValidEclipseName(file);
            if (name.isPresent()) {
               task.from(file, spec -> spec.rename(___ -> name.get()));
            } else {
               project.getLogger().info("Excluding file {} from dependency '{}': not an OSGi bundle",
                        file, identifier.getDisplayName());
            }
         });
         task.into(pluginsDirectory);
      });
      tasks.create(CREATE_METADATA_TASK_NAME, CreateMetadataTask.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Creates metadata for the update site");
         task.dependsOn(UNZIP_ECLIPSE_TASK_NAME, ECLIPSE_CREATE_FEATURES_TASK_NAME,
                  COPY_PLUGINS_TASK_NAME, tasks.withType(MirrorP2Task.class));
         task.getEclipseExecutable().set(baseExtension.getExecutable());
         task.getUpdateSiteDirectory().set(extension.getUpdateSiteDirectory());
      });
      Task xml = tasks.create("createCategoryXml", task -> {
         task.getInputs().property("categories", extension.getCategories());
         File categoryXml = new File(task.getTemporaryDir(), "category.xml");
         task.getOutputs().file(categoryXml);
         task.dependsOn(UNZIP_ECLIPSE_TASK_NAME, ECLIPSE_CREATE_FEATURES_TASK_NAME,
                  COPY_PLUGINS_TASK_NAME, tasks.withType(MirrorP2Task.class));
         task.doLast(__ -> {
            try (Writer writer = Files.newBufferedWriter(categoryXml.toPath())) {
               EclipseCategory.toXml(extension.getCategories().get(), writer);
            } catch (IOException e) {
               throw new TaskExecutionException(task, e);
            }
         });
      });
      tasks.create(PUBLISH_CATEGORY_TASK_NAME, PublishCategoryTask.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Publishes the category for the update site");
         task.dependsOn(xml);
         task.getUpdateSiteDirectory().set(extension.getUpdateSiteDirectory());
         task.getEclipseExecutable().set(baseExtension.getExecutable());
         task.getCategory().set(xml.getOutputs().getFiles().getSingleFile());
      });
      tasks.create(CREATE_UPDATE_SITE_ZIP_TASK_NAME, Zip.class, task -> {
         task.setGroup(ECLIPSE_TASK_GROUP_NAME);
         task.setDescription("Zips the update site");
         task.dependsOn(CREATE_METADATA_TASK_NAME, PUBLISH_CATEGORY_TASK_NAME);
         project.afterEvaluate(__ -> {
            task.from(extension.getUpdateSiteDirectory().get().getAsFile());
            task.setDestinationDir(extension.getUpdateSiteArchive().get().getAsFile().getParentFile());
            task.setArchiveName(extension.getUpdateSiteArchive().get().getAsFile().getName());
         });
      });
      tasks.getByName(LifecycleBasePlugin.BUILD_TASK_NAME)
               .dependsOn(tasks.getByName(CREATE_UPDATE_SITE_ZIP_TASK_NAME));
   }

}
